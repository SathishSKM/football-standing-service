package com.ps.footballstanding.client;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@Slf4j
public class ApiFootballClient implements FootballClient{

    private final RestTemplate restTemplate;

    @Value("${client.api.football.url}")
    private String apiUrl;

    @Value("${client.api.football.key}")
    private String apiKey;

    @Autowired
    public ApiFootballClient(@Qualifier("apiFootballRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    @CircuitBreaker(name = "football-leagues")
    @Bulkhead(name ="football-leagues", type = Bulkhead.Type.SEMAPHORE)
    public List<Country> getCountries() {
        log.info("Fetching countries");
        return exchange(buildUrl("get_countries"), new ParameterizedTypeReference<>() {});
    }

    @Override
    @CircuitBreaker(name = "football-leagues")
    @Bulkhead(name ="football-leagues", type = Bulkhead.Type.SEMAPHORE)
    public List<Standing> getStandings(String leagueId) {
        log.info("Fetching standings for leagueId={}", leagueId);
        return exchange(buildUrl("get_standings", "league_id", leagueId),
                new ParameterizedTypeReference<>() {});
    }

    @CircuitBreaker(name = "football-leagues")
    @Bulkhead(name ="football-leagues", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public List<League> getLeaguesByCountry(String countryId) {
        log.info("Fetching leagues for countryId={}", countryId);
        return exchange(buildUrl("get_leagues", "country_id", countryId),
                new ParameterizedTypeReference<>() {});
    }

    private String buildUrl(String action) {
        return UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("APIkey", apiKey)
                .queryParam("action", action)
                .build().toUriString();
    }

    private String buildUrl(String action, String paramKey, String paramValue) {
        return UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("APIkey", apiKey)
                .queryParam("action", action)
                .queryParam(paramKey, paramValue)
                .build().toUriString();
    }



    private <T> List<T> exchange(String url, ParameterizedTypeReference<List<T>> responseType) {
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, responseType);
            List<T> body = response.getBody();
            return body != null ? body : List.of();
        } catch (RestClientException e) {
            log.error("ApiFootBall HTTP call failed: {}", e.getMessage());
            // Re-throw so Retry → CircuitBreaker can record the failure
            throw new RuntimeException("HTTP call failed for ApiFootBall", e);
        }catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException("HTTP call failed for ApiFootBall", ex);
        }
    }

}
