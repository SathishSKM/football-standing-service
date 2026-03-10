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
import org.springframework.stereotype.Service;

import java.util.List;
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

        try {
            List<Country> countriesFromCache = footballCache.getCachedCountries();
            if (countriesFromCache != null) {
                log.debug("getCountries: serving from cache (offlineMode={})", offlineMode);
                return countriesFromCache;
            }
        } catch (Exception e) {
            log.warn("getCountries: cache error ({}), falling back to API", e.getMessage());
        }

        if (offlineMode) {
            log.info("getCountries: offline + cache miss — using static data");
            return staticDataLoader.getCountries();
        }

        try {
            List<Country> countries = footballClient.getCountries();
            footballCache.cacheCountries(countries);
            return countries;
        } catch (Exception e) {
            log.warn("getCountries: client API failed ({}), using static data", e.getMessage());
            return staticDataLoader.getCountries();
        }
    }

    public List<League> getLeaguesByCountry(String countryId, Boolean offlineMode) {


        if (countryId == null) {
            return List.of();
        }

        try {
            List<League> LeaguesFromCache = footballCache.getCachedLeagues(countryId);
            if (LeaguesFromCache != null) {
                log.debug("getLeaguesByCountry: serving from cache (offlineMode={})", offlineMode);
                return LeaguesFromCache;
            }
        } catch (Exception e) {
            log.warn("getLeaguesByCountry: cache error ({}), falling back to API", e.getMessage());
        }

        if (offlineMode) {
            log.info("getLeagues: offline + cache miss — using static data for countryId={}", countryId);
            return staticDataLoader.getLeagues(countryId);
        }

        try {
            List<League> leagues = footballClient.getLeaguesByCountry(countryId);
            footballCache.cacheLeagues(countryId, leagues);
            return leagues;
        } catch (Exception e) {
            log.warn("getLeagues: live API failed ({}), using static data", e.getMessage());
            return staticDataLoader.getLeagues(countryId);
        }
    }

    public List<StandingResponse> getStandings(String countryId, String leagueId, String team, boolean offlineMode) {

        List<League> allLeagues = getLeaguesByCountry(countryId, offlineMode);
        List<League> targetLeagues = (leagueId == null || leagueId.isBlank())
                ? allLeagues
                : allLeagues.stream()
                .filter(l -> l.getLeagueId().equalsIgnoreCase(leagueId))
                .toList();

        if (targetLeagues.isEmpty()) {
            log.warn("getStandings: no leagues matched (country={}, league={})", countryId, leagueId);
            return List.of();
        }

        return targetLeagues.stream()
                .flatMap(l -> fetchStandingsForLeague(l.getLeagueId(), offlineMode).stream())
                .filter(s -> team == null || team.isBlank()
                        || s.getTeamName().equalsIgnoreCase(team))
                .collect(Collectors.toList());

    }

    private List<StandingResponse> fetchStandingsForLeague(String leagueId, boolean offlineMode) {

        List<Standing> standings;

        if (offlineMode) {
            standings = getStandingsFromCacheOrStatic(leagueId);
        } else {
            standings = getStandingsFromClientCacheOrStatic(leagueId);
        }

        return standings.stream()
                .map(s -> toResponse(s, offlineMode))
                .collect(Collectors.toList());
    }

    private List<Standing> getStandingsFromClientCacheOrStatic(String leagueId) {

        try {
            List<Standing> standings = footballClient.getStandings(leagueId);
            cacheStandings(leagueId, standings);
            return standings;

        } catch (Exception clientError) {
            log.warn("Client API failed for leagueId={}, trying cache", leagueId, clientError);

            List<Standing> cache = getStandingsFromCache(leagueId);
            if (cache != null) {
                log.info("Serving stale cache for leagueId={}", leagueId);
                return cache;
            }

            log.warn("Cache unavailable for leagueId={}, using static data", leagueId);
            return staticDataLoader.getStandings(leagueId);
        }
    }

    private List<Standing> getStandingsFromCacheOrStatic(String leagueId) {

        List<Standing> cache = getStandingsFromCache(leagueId);

        if (cache != null) {
            log.info("Offline mode: serving cache for leagueId={}", leagueId);
            return cache;
        }

        log.info("Offline mode: cache miss, using static data for leagueId={}", leagueId);
        return staticDataLoader.getStandings(leagueId);
    }

    private List<Standing> getStandingsFromCache(String leagueId) {
        try {
            return footballCache.getCachedStandings(leagueId);
        } catch (Exception e) {
            log.warn("Cache read failed for leagueId={}", leagueId, e);
            return null;
        }
    }

    private void cacheStandings(String leagueId, List<Standing> standings) {
        try {
            footballCache.cacheStandings(leagueId, standings);
        } catch (Exception e) {
            log.warn("Cache write failed for leagueId={}", leagueId, e);
        }
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
