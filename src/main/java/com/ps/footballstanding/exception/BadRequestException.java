package com.ps.footballstanding.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends FootballServiceException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}
