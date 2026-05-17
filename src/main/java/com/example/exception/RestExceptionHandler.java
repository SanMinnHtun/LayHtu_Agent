package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<?> handleNoData(NoDataException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "no_data");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExternalProcessTimeoutException.class)
    public ResponseEntity<?> handleTimeout(ExternalProcessTimeoutException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "timeout");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body);
    }

    @ExceptionHandler(ModelExecutionException.class)
    public ResponseEntity<?> handleModel(ModelExecutionException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "model_error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "internal_error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

