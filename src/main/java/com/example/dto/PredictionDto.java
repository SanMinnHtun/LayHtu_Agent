package com.example.dto;

public class PredictionDto {
    private String time;
    private Double observedPm25;
    private Double predictedPm25;
    private Double confidence;
    private String label;
    private String iconUrl;

    public PredictionDto() {}

    public PredictionDto(String time, Double observedPm25, Double predictedPm25, Double confidence, String label, String iconUrl) {
        this.time = time;
        this.observedPm25 = observedPm25;
        this.predictedPm25 = predictedPm25;
        this.confidence = confidence;
        this.label = label;
        this.iconUrl = iconUrl;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public Double getObservedPm25() { return observedPm25; }
    public void setObservedPm25(Double observedPm25) { this.observedPm25 = observedPm25; }
    public Double getPredictedPm25() { return predictedPm25; }
    public void setPredictedPm25(Double predictedPm25) { this.predictedPm25 = predictedPm25; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}

