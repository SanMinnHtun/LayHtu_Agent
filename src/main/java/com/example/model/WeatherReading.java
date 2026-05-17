package com.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "weather_readings")
public class WeatherReading {
    @Id
    private String id;
    private Instant timestamp;
    private Double temperature;
    private Double humidity;
    private Double pm25;

    public WeatherReading() {}

    public WeatherReading(String id, Instant timestamp, Double temperature, Double humidity, Double pm25) {
        this.id = id;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pm25 = pm25;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Double getPm25() { return pm25; }
    public void setPm25(Double pm25) { this.pm25 = pm25; }
}

