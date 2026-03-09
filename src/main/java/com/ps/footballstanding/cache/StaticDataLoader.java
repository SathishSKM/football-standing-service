package com.ps.footballstanding.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads static bundled fallback JSON once at startup.
 * Used as last resort when offlineMode=true AND cache is empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaticDataLoader {

    private final ObjectMapper objectMapper;

    private List<Country>  staticCountries  = Collections.emptyList();
    private JsonNode        staticLeagues;
    private JsonNode        staticStandings;

    @PostConstruct
    public void load() {
        try {
            InputStream is = new ClassPathResource("offline/fallback-data.json").getInputStream();
            JsonNode root = objectMapper.readTree(is);

            // Countries
            List<Country> countries = new ArrayList<>();
            for (JsonNode n : root.get("countries"))
                countries.add(objectMapper.convertValue(n, Country.class));
            staticCountries = countries;

            staticLeagues   = root.get("leagues");
            staticStandings = root.get("standings");

            log.info("Static fallback data loaded: {} countries", countries.size());
        } catch (Exception e) {
            log.error("Could not load static fallback data: {}", e.getMessage());
        }
    }

    public List<Country> getCountries() {
        return staticCountries;
    }

    public List<League> getLeagues(String countryId) {
        if (staticLeagues == null) return Collections.emptyList();
        JsonNode node = staticLeagues.get(countryId);
        if (node == null) return Collections.emptyList();
        List<League> list = new ArrayList<>();
        for (JsonNode n : node) list.add(objectMapper.convertValue(n, League.class));
        return list;
    }

    public List<Standing> getStandings(String leagueId) {
        if (staticStandings == null) return Collections.emptyList();
        JsonNode node = staticStandings.get(leagueId);
        if (node == null) return Collections.emptyList();
        List<Standing> list = new ArrayList<>();
        for (JsonNode n : node) list.add(objectMapper.convertValue(n, Standing.class));
        return list;
    }
}
