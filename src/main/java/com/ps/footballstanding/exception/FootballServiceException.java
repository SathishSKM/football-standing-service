package com.ps.footballstanding.exception;

import org.springframework.http.HttpStatus;

public class FootballServiceException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public FootballServiceException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
