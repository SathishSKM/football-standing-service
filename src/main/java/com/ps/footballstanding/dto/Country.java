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
public class Country {

    @JsonAlias("country_id")
    private String countryId;

    @JsonAlias("country_name")
    private String countryName;

    @JsonAlias("country_logo")
    private String countryLogo;

}
