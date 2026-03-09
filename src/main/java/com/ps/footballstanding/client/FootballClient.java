package com.ps.footballstanding.client;

import com.ps.footballstanding.dto.Country;
import com.ps.footballstanding.dto.League;
import com.ps.footballstanding.dto.Standing;

import java.util.List;

public interface FootballClient {

    List<Standing> getStandings(String leagueId);

    List<Country> getCountries();

    List<League> getLeaguesByCountry(String countryId);
}
