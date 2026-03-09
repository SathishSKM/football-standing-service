package com.ps.footballstanding.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends FootballServiceException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }
}
