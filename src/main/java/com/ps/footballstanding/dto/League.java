package com.ps.footballstanding.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class League {
    @JsonAlias("league_id")    
    private String leagueId;
    
    @JsonAlias("league_name")   
    private String leagueName;
    
    @JsonAlias("country_id")    
    private String countryId;
    
    @JsonAlias("country_name")  
    private String countryName;
    
    @JsonAlias("league_logo")   
    private String leagueLogo;
    
    @JsonAlias("league_season") 
    private String leagueSeason;
}
