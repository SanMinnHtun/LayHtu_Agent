package com.example.exception;

public class ExternalProcessTimeoutException extends RuntimeException {
    public ExternalProcessTimeoutException(String message) { super(message); }
    public ExternalProcessTimeoutException(String message, Throwable cause) { super(message, cause); }
}

