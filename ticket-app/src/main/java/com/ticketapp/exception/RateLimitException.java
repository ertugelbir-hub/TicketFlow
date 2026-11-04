package com.ticketapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends RuntimeException {
    private final long retryAfter; // Saniye bilgisini tutacak değişken

    public RateLimitException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }


    public long getRetryAfter() {
        return retryAfter;
    }
}