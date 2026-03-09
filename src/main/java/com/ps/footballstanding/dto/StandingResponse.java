package com.ps.footballstanding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StandingResponse {
    private String countryName;
    private String leagueId;
    private String leagueName;
    private String teamId;
    private String teamName;
    private String teamBadge;
    private String overallLeaguePosition;
    private String played;
    private String wins;
    private String draws;
    private String losses;
    private String goalsFor;
    private String goalsAgainst;
    private String points;
    private String overallPromotion;
    private String homeLeaguePosition;
    private String awayLeaguePosition;
    private boolean offlineMode;
    private String resolvedProvider;

    // HATEOAS links
    private Links links;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Links {
        private String self;
        private String league;
        private String country;
    }
}
