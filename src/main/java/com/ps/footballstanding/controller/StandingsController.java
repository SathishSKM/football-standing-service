package com.ps.footballstanding.controller;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;
import com.ps.footballstanding.dto.StandingResponse;
import com.ps.footballstanding.service.StandingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class StandingsController {

    private final StandingsService standingsService;

    @Autowired
    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping("/countries")
    @Operation(summary = "List all countries")
    public ResponseEntity<List<Country>> getCountries(@RequestParam(defaultValue = "false") boolean offlineMode) {
        return ResponseEntity.ok(standingsService.getCountries(offlineMode));
    }

    @GetMapping("/leagues")
    @Operation(summary = "Get leagues by country name")
    public ResponseEntity<List<League>> getLeagues(
            @RequestParam String countryId, @RequestParam(defaultValue = "false") boolean offlineMode) {

        return ResponseEntity.ok(
                standingsService.getLeaguesByCountry(countryId, offlineMode));
    }

    @GetMapping("/standings")
    @Operation(summary = "Get standing for a specific team")
    public ResponseEntity<List<StandingResponse>> getStandings(
            @RequestParam String countryId,
            @RequestParam(required = false) String leagueId,
            @RequestParam(required = false) String team,
            @RequestParam(defaultValue = "false") boolean offlineMode) {

        log.info("Standing request: country={}, league={}, team={}", countryId, leagueId, team);

        List<StandingResponse> standing = standingsService.getStandings(countryId, leagueId, team, offlineMode);


        return ResponseEntity.ok(standing);
    }

}
