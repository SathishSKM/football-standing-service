package com.ps.footballstanding.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String errorCode;
    private final String message;
    private final String path;
    private final int status;

    public ErrorResponse(FootballServiceException ex, String path) {
        this.timestamp = LocalDateTime.now();
        this.errorCode = ex.getErrorCode();
        this.message = ex.getMessage();
        this.path = path;
        this.status = ex.getHttpStatus().value();
    }
}
