package com.example.controller;

import com.example.service.AirQualityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    private final AirQualityService airQualityService;

    public WeatherController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    @GetMapping("/predict")
    public ResponseEntity<?> predict() {
        logger.info("Received weather prediction request");
        Optional<Double> maybePrediction = airQualityService.predictFromLast24();
        if (maybePrediction.isPresent()) {
            Double prediction = maybePrediction.get();
            return ResponseEntity.ok(new com.example.controller.dto.PredictionResponse(prediction));
        } else {
            com.example.controller.dto.ErrorResponse err = new com.example.controller.dto.ErrorResponse("Not enough data or model error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}
