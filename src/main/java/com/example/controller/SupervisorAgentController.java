package com.example.controller;

import com.example.dto.ForecastResponseDto;
import com.example.service.ForecastService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supervisor")
public class SupervisorAgentController {

    private final ForecastService forecastService;

    public SupervisorAgentController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping(value = "/forecast", produces = MediaType.APPLICATION_JSON_VALUE)
    public ForecastResponseDto getForecast() {
        return forecastService.generateForecast();
    }
}

