package com.example.service;

import com.example.controller.dto.CityDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherProviderService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherProviderService.class);

    @Value("${openweather.api.key:}")
    private String openWeatherApiKey;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetch PM2.5 values for a small set of cities using OpenWeather Air Pollution API.
     * If an API key is not configured, return an empty list so callers may fallback to demo data.
     */
    public List<CityDto> fetchCitiesData() {
        List<CityDto> out = new ArrayList<>();

        // If API key missing, return empty and let controller provide demo fallback
        if (openWeatherApiKey == null || openWeatherApiKey.isBlank()) {
            logger.info("OpenWeather API key not configured; returning empty city list for fallback");
            return out;
        }

        // Coordinates: Yangon, Mandalay, Naypyidaw
        try {
            Double yPm = fetchPm25(16.8409, 96.1735);
            out.add(new CityDto("Yangon", "Urban centre", yPm));
        } catch (Exception e) {
            logger.warn("Failed to fetch Yangon PM2.5", e);
            out.add(new CityDto("Yangon", "Urban centre", null));
        }

        try {
            Double mPm = fetchPm25(21.9586, 96.0891);
            out.add(new CityDto("Mandalay", "Inland region", mPm));
        } catch (Exception e) {
            logger.warn("Failed to fetch Mandalay PM2.5", e);
            out.add(new CityDto("Mandalay", "Inland region", null));
        }

        try {
            Double nPm = fetchPm25(19.7633, 96.0785);
            out.add(new CityDto("Naypyidaw", "Capital area", nPm));
        } catch (Exception e) {
            logger.warn("Failed to fetch Naypyidaw PM2.5", e);
            out.add(new CityDto("Naypyidaw", "Capital area", null));
        }

        return out;
    }

    private Double fetchPm25(double lat, double lon) {
        String url = String.format("http://api.openweathermap.org/data/2.5/air_pollution?lat=%f&lon=%f&appid=%s", lat, lon, openWeatherApiKey);
        try {
            String json = restTemplate.getForObject(url, String.class);
            if (json == null) {
                logger.warn("OpenWeather returned empty body for {},{}", lat, lon);
                return null;
            }
            JsonNode root = mapper.readTree(json);
            JsonNode list = root.path("list");
            if (list.isArray() && list.size() > 0) {
                JsonNode first = list.get(0);
                JsonNode components = first.path("components");
                if (components.has("pm2_5")) {
                    return components.get("pm2_5").asDouble();
                }
            }
        } catch (RestClientException rce) {
            logger.warn("RestTemplate error when calling OpenWeather", rce);
        } catch (Exception e) {
            logger.warn("Error parsing OpenWeather response", e);
        }
        return null;
    }
}
