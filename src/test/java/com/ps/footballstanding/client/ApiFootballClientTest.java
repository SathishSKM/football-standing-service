package com.ps.footballstanding.client;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiFootballClientTest {

    @Mock
    private RestTemplate restTemplate;

    private ApiFootballClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new ApiFootballClient(restTemplate);
        try {
            var apiUrlField = ApiFootballClient.class.getDeclaredField("apiUrl");
            apiUrlField.setAccessible(true);
            apiUrlField.set(client, "http://fake-url");
            var apiKeyField = ApiFootballClient.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(client, "fake-key");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Given valid response, when getCountries called, then returns countries")
    void shouldReturnCountries_whenGetCountriesCalled() {
        List<Country> mockCountries = List.of(new Country());
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<Country>>>any()))
                .thenReturn(ResponseEntity.ok(mockCountries));

        List<Country> result = client.getCountries();

        assertThat(result).isEqualTo(mockCountries);
    }

    @Test
    @DisplayName("Given RestClientException, when getCountries called, then throws RuntimeException")
    void shouldThrow_whenGetCountriesFails() {
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<Country>>>any()))
                .thenThrow(new RestClientException("error"));

        assertThatThrownBy(() -> client.getCountries())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HTTP call failed");
    }

    @Test
    @DisplayName("Given valid response, when getStandings called, then returns standings")
    void shouldReturnStandings_whenGetStandingsCalled() {
        List<Standing> mockStandings = List.of(new Standing());
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<Standing>>>any()))
                .thenReturn(ResponseEntity.ok(mockStandings));

        List<Standing> result = client.getStandings("123");

        assertThat(result).isEqualTo(mockStandings);
    }

    @Test
    @DisplayName("Given RestClientException, when getStandings called, then throws RuntimeException")
    void shouldThrow_whenGetStandingsFails() {
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<Standing>>>any()))
                .thenThrow(new RestClientException("error"));

        assertThatThrownBy(() -> client.getStandings("123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HTTP call failed");
    }

    @Test
    @DisplayName("Given valid response, when getLeaguesByCountry called, then returns leagues")
    void shouldReturnLeagues_whenGetLeaguesByCountryCalled() {
        List<League> mockLeagues = List.of(new League());
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<League>>>any()))
                .thenReturn(ResponseEntity.ok(mockLeagues));

        List<League> result = client.getLeaguesByCountry("456");

        assertThat(result).isEqualTo(mockLeagues);
    }

    @Test
    @DisplayName("Given RestClientException, when getLeaguesByCountry called, then throws RuntimeException")
    void shouldThrow_whenGetLeaguesByCountryFails() {
        when(restTemplate.exchange(anyString(), any(), any(), ArgumentMatchers.<ParameterizedTypeReference<List<League>>>any()))
                .thenThrow(new RestClientException("error"));

        assertThatThrownBy(() -> client.getLeaguesByCountry("456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HTTP call failed");
    }
}

