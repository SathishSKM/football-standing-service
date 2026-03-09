package com.ps.footballstanding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FootballStandingServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(FootballStandingServiceApplication.class, args);
	}

}
