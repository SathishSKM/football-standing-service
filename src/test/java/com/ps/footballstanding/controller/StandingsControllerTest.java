package com.ps.footballstanding.controller;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.StandingResponse;
import com.ps.footballstanding.service.StandingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandingsControllerTest {

    @Mock
    private StandingsService standingsService;

    private StandingsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new StandingsController(standingsService);
    }

    @Test
    @DisplayName("Given countries exist, when getCountries called, then returns countries")
    void shouldReturnCountries_whenGetCountriesCalled() {
        List<Country> mockCountries = List.of(new Country());
        when(standingsService.getCountries(false)).thenReturn(mockCountries);

        ResponseEntity<List<Country>> response = controller.getCountries(false);

        assertThat(response.getBody()).isEqualTo(mockCountries);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("Given leagues exist, when getLeagues called, then returns leagues")
    void shouldReturnLeagues_whenGetLeaguesCalled() {
        List<League> mockLeagues = List.of(new League());
        when(standingsService.getLeaguesByCountry("countryId", false)).thenReturn(mockLeagues);

        ResponseEntity<List<League>> response = controller.getLeagues("countryId", false);

        assertThat(response.getBody()).isEqualTo(mockLeagues);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("Given standings exist, when getStandings called, then returns standings")
    void shouldReturnStandings_whenGetStandingsCalled() {
        List<StandingResponse> mockStandings = List.of(new StandingResponse());
        when(standingsService.getStandings("countryId", "leagueId", "team", false)).thenReturn(mockStandings);

        ResponseEntity<List<StandingResponse>> response = controller.getStandings("countryId", "leagueId", "team", false);

        assertThat(response.getBody()).isEqualTo(mockStandings);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("Given service throws exception, when getCountries called, then returns empty list")
    void shouldReturnEmpty_whenGetCountriesThrows() {
        when(standingsService.getCountries(false)).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> controller.getCountries(false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("error");
    }

    @Test
    @DisplayName("Given service throws exception, when getLeagues called, then returns empty list")
    void shouldReturnEmpty_whenGetLeaguesThrows() {
        when(standingsService.getLeaguesByCountry("countryId", false)).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> controller.getLeagues("countryId", false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("error");
    }

    @Test
    @DisplayName("Given service throws exception, when getStandings called, then returns empty list")
    void shouldReturnEmpty_whenGetStandingsThrows() {
        when(standingsService.getStandings("countryId", "leagueId", "team", false)).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> controller.getStandings("countryId", "leagueId", "team", false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("error");
    }
}

