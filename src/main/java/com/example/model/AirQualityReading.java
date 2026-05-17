package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "air_quality_readings")
public class AirQualityReading {
    @Id
    private String id;
    private Instant timestamp;
    private Double pm25;

    public AirQualityReading() {}

    public AirQualityReading(String id, Instant timestamp, Double pm25) {
        this.id = id;
        this.timestamp = timestamp;
        this.pm25 = pm25;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getPm25() { return pm25; }
    public void setPm25(Double pm25) { this.pm25 = pm25; }
}

