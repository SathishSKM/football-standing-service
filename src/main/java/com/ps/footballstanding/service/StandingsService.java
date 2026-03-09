package com.ps.footballstanding.service;


import com.ps.footballstanding.cache.FootballCache;
import com.ps.footballstanding.cache.StaticDataLoader;
import com.ps.footballstanding.client.FootballClient;
import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import com.ps.footballstanding.dto.StandingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class StandingsService {

    private final FootballClient footballClient;

    private final FootballCache footballCache;

    private final StaticDataLoader staticDataLoader;


    @Autowired
    public StandingsService(@Qualifier("apiFootballClient") FootballClient footballClient, FootballCache footballCache, StaticDataLoader staticDataLoader) {
        this.footballClient = footballClient;
        this.footballCache = footballCache;
        this.staticDataLoader = staticDataLoader;
    }

    public List<Country> getCountries(boolean offlineMode) {

        // 1. Always try cache first
        List<Country> cached = footballCache.getCachedCountries();
        if (cached != null) {
            log.debug("getCountries: serving from cache (offlineMode={})", offlineMode);
            return cached;
        }

        // 2. Offline — cache miss means no data available, use static
        if (offlineMode) {
            log.info("getCountries: offline + cache miss — using static data");
            return staticDataLoader.getCountries();
        }

        // 3. Online — call live API, cache result
        try {
            List<Country> live = footballClient.getCountries();
            footballCache.cacheCountries(live);
            return live;
        } catch (Exception e) {
            log.warn("getCountries: live API failed ({}), using static data", e.getMessage());
            return staticDataLoader.getCountries();
        }
    }

    public List<League> getLeaguesByCountry(String countryId, Boolean offlineMode) {


        if (countryId == null) return List.of();

        // 1. Try cache
        List<League> cached = footballCache.getCachedLeagues(countryId);
        if (cached != null) {
            log.debug("getLeagues: serving from cache for countryId={}", countryId);
            return cached;
        }

        // 2. Offline — no cache, use static
        if (offlineMode) {
            log.info("getLeagues: offline + cache miss — using static data for countryId={}", countryId);
            return staticDataLoader.getLeagues(countryId);
        }

        // 3. Live API, cache result
        try {
            List<League> live = footballClient.getLeaguesByCountry(countryId);
            footballCache.cacheLeagues(countryId, live);
            return live;
        } catch (Exception e) {
            log.warn("getLeagues: live API failed ({}), using static data", e.getMessage());
            return staticDataLoader.getLeagues(countryId);
        }
    }

    public List<StandingResponse> getStandings(String countryId, String leagueId, String team, boolean offlineMode) {


        // 2. Get leagues for country, filter by league name if provided
        List<League> allLeagues = getLeaguesById(countryId, offlineMode);
        List<League> targetLeagues = (leagueId == null || leagueId.isBlank())
                ? allLeagues
                : allLeagues.stream()
                .filter(l -> l.getLeagueId().equalsIgnoreCase(leagueId))
                .toList();

        if (targetLeagues.isEmpty()) {
            log.warn("getStandings: no leagues matched (country={}, league={})", countryId, leagueId);
            return List.of();
        }

        // 3. Fetch standings per league, map to DTO, filter by team if provided
        return targetLeagues.stream()
                .flatMap(l -> fetchStandingsForLeague(l.getLeagueId(), offlineMode).stream())
                .filter(s -> team == null || team.isBlank()
                        || s.getTeamName().equalsIgnoreCase(team))
                .collect(Collectors.toList());

    }


    /** Used internally by getStandings — bypasses country-name resolution. */
    private List<League> getLeaguesById(String countryId, boolean offlineMode) {
        List<League> cached = footballCache.getCachedLeagues(countryId);
        if (cached != null) return cached;

        if (offlineMode) return staticDataLoader.getLeagues(countryId);

        try {
            List<League> live = footballClient.getLeaguesByCountry(countryId);
            footballCache.cacheLeagues(countryId, live);
            return live;
        } catch (Exception e) {
            log.warn("getLeaguesById: live API failed for countryId={}", countryId);
            return staticDataLoader.getLeagues(countryId);
        }
    }

    private List<StandingResponse> fetchStandingsForLeague(String leagueId, boolean offlineMode) {
        List<Standing> raw;
        boolean usedOffline;

        if (!offlineMode) {
            // ── Online: live API first → on failure fall back to cache → then static
            try {
                raw = footballClient.getStandings(leagueId);
                footballCache.cacheStandings(leagueId, raw);   // refresh cache on success
                usedOffline = false;
            } catch (Exception e) {
                log.warn("fetchStandings: live API failed for leagueId={}, trying cache", leagueId);
                List<Standing> cached = footballCache.getCachedStandings(leagueId);
                if (cached != null) {
                    log.info("fetchStandings: serving stale cache for leagueId={}", leagueId);
                    raw = cached;
                } else {
                    log.warn("fetchStandings: cache empty for leagueId={}, using static data", leagueId);
                    raw = staticDataLoader.getStandings(leagueId);
                }
                usedOffline = true;
            }
        } else {
            // ── Offline: cache first → on miss fall back to static
            List<Standing> cached = footballCache.getCachedStandings(leagueId);
            if (cached != null) {
                log.info("fetchStandings: offline — serving from cache for leagueId={}", leagueId);
                raw = cached;
            } else {
                log.info("fetchStandings: offline + cache miss — using static data for leagueId={}", leagueId);
                raw = staticDataLoader.getStandings(leagueId);
            }
            usedOffline = true;
        }

        final boolean flagOffline = usedOffline;
        return raw.stream()
                .map(s -> toResponse(s, flagOffline))
                .collect(Collectors.toList());
    }



    private StandingResponse toResponse(Standing s, boolean offlineMode) {
        return StandingResponse.builder()
                .countryName(s.getCountryName())
                .leagueId(s.getLeagueId())
                .leagueName(s.getLeagueName())
                .teamId(s.getTeamId())
                .teamName(s.getTeamName())
                .teamBadge(s.getTeamBadge())
                .overallLeaguePosition(s.getOverallLeaguePosition())
                .played(s.getOverallLeaguePayed())
                .wins(s.getOverallLeagueW())
                .draws(s.getOverallLeagueD())
                .losses(s.getOverallLeagueL())
                .goalsFor(s.getOverallLeagueGF())
                .goalsAgainst(s.getOverallLeagueGA())
                .points(s.getOverallLeaguePTS())
                .overallPromotion(s.getOverallPromotion())
                .homeLeaguePosition(s.getHomeLeaguePosition())
                .awayLeaguePosition(s.getAwayLeaguePosition())
                .offlineMode(offlineMode)
                .resolvedProvider(offlineMode ? "OFFLINE" : "API")
                .links(StandingResponse.Links.builder()
                        .self("/api/v1/standings?country=" + s.getCountryName()
                                + "&league=" + s.getLeagueName()
                                + "&team=" + s.getTeamName())
                        .league("/api/v1/standings?country=" + s.getCountryName()
                                + "&league=" + s.getLeagueName())
                        .country("/api/v1/countries")
                        .build())
                .build();
    }

}
