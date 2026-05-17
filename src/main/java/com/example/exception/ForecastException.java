package com.example.exception;

public class ForecastException extends RuntimeException {
    public ForecastException(String message) { super(message); }
    public ForecastException(String message, Throwable cause) { super(message, cause); }
}

