package com.example.dto;

public class HealthAdviceDto {
    private String level; // e.g., "Good", "Moderate", "Unhealthy"
    private String advice;

    public HealthAdviceDto() {}

    public HealthAdviceDto(String level, String advice) {
        this.level = level;
        this.advice = advice;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
}

