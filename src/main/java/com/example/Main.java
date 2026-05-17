package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Lay Htu Spring Boot application.
 * Starts the Spring context and embedded server.
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        // Simple console confirmation for presentations and quick checks
        System.out.println("Lay Htu backend is live - Spring Boot started successfully.");
    }
}