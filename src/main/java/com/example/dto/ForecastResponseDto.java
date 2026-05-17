package com.example.dto;

import java.util.List;

public class ForecastResponseDto {
    private List<PredictionDto> predictions;
    private List<ActionDto> actions;
    private List<HealthAdviceDto> healthAdvice;

    public ForecastResponseDto() {}

    public ForecastResponseDto(List<PredictionDto> predictions, List<ActionDto> actions, List<HealthAdviceDto> healthAdvice) {
        this.predictions = predictions;
        this.actions = actions;
        this.healthAdvice = healthAdvice;
    }

    public List<PredictionDto> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionDto> predictions) { this.predictions = predictions; }

    public List<ActionDto> getActions() { return actions; }
    public void setActions(List<ActionDto> actions) { this.actions = actions; }

    public List<HealthAdviceDto> getHealthAdvice() { return healthAdvice; }
    public void setHealthAdvice(List<HealthAdviceDto> healthAdvice) { this.healthAdvice = healthAdvice; }
}

