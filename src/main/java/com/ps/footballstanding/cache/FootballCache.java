package com.ps.footballstanding.cache;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FootballCache {

    @Cacheable(value = "countries", key = "'all'", unless = "#result == null")
    public List<Country> getCachedCountries() {
        log.info("Cache MISS — countries not cached yet");
        return null;
    }

    @CachePut(value = "countries", key = "'all'")
    public List<Country> cacheCountries(List<Country> countries) {
        log.debug("Cache PUT — {} countries stored", countries.size());
        return countries;
    }

    @Cacheable(value = "leagues", key = "#countryId", unless = "#result == null")
    public List<League> getCachedLeagues(String countryId) {
        log.info("Cache MISS — leagues not cached for countryId={}", countryId);
        return null;
    }

    @CachePut(value = "leagues", key = "#countryId")
    public List<League> cacheLeagues(String countryId, List<League> leagues) {
        log.debug("Cache PUT — {} leagues stored for countryId={}", leagues.size(), countryId);
        return leagues;
    }


    @Cacheable(value = "standings", key = "#leagueId", unless = "#result == null")
    public List<Standing> getCachedStandings(String leagueId) {
        log.info("Cache MISS — standings not cached for leagueId={}", leagueId);
        return null;
    }

    @CachePut(value = "standings", key = "#leagueId")
    public List<Standing> cacheStandings(String leagueId, List<Standing> standings) {
        log.debug("Cache PUT — {} standings stored for leagueId={}", standings.size(), leagueId);
        return standings;
    }
}
