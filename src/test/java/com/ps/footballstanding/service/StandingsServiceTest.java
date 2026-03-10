package com.ps.footballstanding.service;

import com.ps.footballstanding.cache.FootballCache;
import com.ps.footballstanding.cache.StaticDataLoader;
import com.ps.footballstanding.client.FootballClient;
import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import com.ps.footballstanding.dto.StandingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandingsServiceTest {

    @Mock
    private FootballClient footballClient;
    @Mock
    private FootballCache footballCache;
    @Mock
    private StaticDataLoader staticDataLoader;

    private StandingsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new StandingsService(footballClient, footballCache, staticDataLoader);
    }

    @Test
    @DisplayName("Given cache has countries, when getCountries called, then returns cached countries")
    void shouldReturnCachedCountries_whenCacheAvailable() {
        List<Country> cached = List.of(new Country());
        when(footballCache.getCachedCountries()).thenReturn(cached);

        List<Country> result = service.getCountries(false);

        assertThat(result).isEqualTo(cached);
    }

    @Test
    @DisplayName("Given offline mode and cache miss, when getCountries called, then returns static countries")
    void shouldReturnStaticCountries_whenOfflineAndCacheMiss() {
        when(footballCache.getCachedCountries()).thenReturn(null);
        List<Country> staticCountries = List.of(new Country());
        when(staticDataLoader.getCountries()).thenReturn(staticCountries);

        List<Country> result = service.getCountries(true);

        assertThat(result).isEqualTo(staticCountries);
    }

    @Test
    @DisplayName("Given live API succeeds, when getCountries called, then returns live countries and caches them")
    void shouldReturnLiveCountries_whenOnlineAndCacheMiss() {
        when(footballCache.getCachedCountries()).thenReturn(null);
        List<Country> liveCountries = List.of(new Country());
        when(footballClient.getCountries()).thenReturn(liveCountries);

        List<Country> result = service.getCountries(false);

        assertThat(result).isEqualTo(liveCountries);
        verify(footballCache).cacheCountries(liveCountries);
    }

    @Test
    @DisplayName("Given live API fails, when getCountries called, then returns static countries")
    void shouldReturnStaticCountries_whenLiveApiFails() {
        when(footballCache.getCachedCountries()).thenReturn(null);
        when(footballClient.getCountries()).thenThrow(new RuntimeException("fail"));
        List<Country> staticCountries = List.of(new Country());
        when(staticDataLoader.getCountries()).thenReturn(staticCountries);

        List<Country> result = service.getCountries(false);

        assertThat(result).isEqualTo(staticCountries);
    }

    @Test
    @DisplayName("Given cache has leagues, when getLeaguesByCountry called, then returns cached leagues")
    void shouldReturnCachedLeagues_whenCacheAvailable() {
        List<League> cached = List.of(new League());
        when(footballCache.getCachedLeagues("countryId")).thenReturn(cached);

        List<League> result = service.getLeaguesByCountry("countryId", false);

        assertThat(result).isEqualTo(cached);
    }

    @Test
    @DisplayName("Given offline mode and cache miss, when getLeaguesByCountry called, then returns static leagues")
    void shouldReturnStaticLeagues_whenOfflineAndCacheMiss() {
        when(footballCache.getCachedLeagues("countryId")).thenReturn(null);
        List<League> staticLeagues = List.of(new League());
        when(staticDataLoader.getLeagues("countryId")).thenReturn(staticLeagues);

        List<League> result = service.getLeaguesByCountry("countryId", true);

        assertThat(result).isEqualTo(staticLeagues);
    }

    @Test
    @DisplayName("Given live API succeeds, when getLeaguesByCountry called, then returns live leagues and caches them")
    void shouldReturnLiveLeagues_whenOnlineAndCacheMiss() {
        when(footballCache.getCachedLeagues("countryId")).thenReturn(null);
        List<League> liveLeagues = List.of(new League());
        when(footballClient.getLeaguesByCountry("countryId")).thenReturn(liveLeagues);

        List<League> result = service.getLeaguesByCountry("countryId", false);

        assertThat(result).isEqualTo(liveLeagues);
        verify(footballCache).cacheLeagues("countryId", liveLeagues);
    }

    @Test
    @DisplayName("Given live API fails, when getLeaguesByCountry called, then returns static leagues")
    void shouldReturnStaticLeagues_whenLiveApiFails() {
        when(footballCache.getCachedLeagues("countryId")).thenReturn(null);
        when(footballClient.getLeaguesByCountry("countryId")).thenThrow(new RuntimeException("fail"));
        List<League> staticLeagues = List.of(new League());
        when(staticDataLoader.getLeagues("countryId")).thenReturn(staticLeagues);

        List<League> result = service.getLeaguesByCountry("countryId", false);

        assertThat(result).isEqualTo(staticLeagues);
    }

    @Test
    @DisplayName("Given null countryId, when getLeaguesByCountry called, then returns empty list")
    void shouldReturnEmpty_whenCountryIdNull() {
        List<League> result = service.getLeaguesByCountry(null, false);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Given leagues and standings exist, when getStandings called, then returns standing responses")
    void shouldReturnStandingResponses_whenGetStandingsCalled() {
        League league = new League();
        league.setLeagueId("leagueId");
        List<League> leagues = List.of(league);
        when(footballCache.getCachedLeagues("countryId")).thenReturn(leagues);

        Standing standing = new Standing();
        standing.setLeagueId("leagueId");
        standing.setTeamName("team");
        standing.setCountryName("country");
        standing.setLeagueName("league");
        List<Standing> standings = List.of(standing);
        when(footballCache.getCachedStandings("leagueId")).thenReturn(standings);

        List<StandingResponse> result = service.getStandings("countryId", "leagueId", "team", true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("team");
    }

    @Test
    @DisplayName("Given no leagues matched, when getStandings called, then returns empty list")
    void shouldReturnEmpty_whenNoLeaguesMatched() {
        when(footballCache.getCachedLeagues("countryId")).thenReturn(List.of());

        List<StandingResponse> result = service.getStandings("countryId", "leagueId", "team", true);

        assertThat(result).isEmpty();
    }
}

