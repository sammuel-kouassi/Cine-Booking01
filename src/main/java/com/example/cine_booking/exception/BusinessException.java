package com.example.cine_booking.exception;

/**
 * Simple unchecked exception to represent business rule violations
 * within the application services layer.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
