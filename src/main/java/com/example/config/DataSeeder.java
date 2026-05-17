package com.example.config;

import com.example.model.AirQualityReading;
import com.example.repository.AirQualityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final AirQualityRepository repository;

    public DataSeeder(AirQualityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long count = repository.count();
        if (count >= 24) {
            logger.info("AirQualityReading collection already has {} records; skipping seeding.", count);
            return;
        }

        logger.info("Seeding AirQualityReading collection with 24 sample records (found {}).", count);
        List<AirQualityReading> samples = new ArrayList<>();
        Instant now = Instant.now();
        // Create 24 hourly samples ending at now (oldest -> newest)
        for (int i = 24; i >= 1; i--) {
            Instant ts = now.minus(Duration.ofHours(i));
            double pm25 = 10 + Math.random() * 40; // sample values between 10-50
            AirQualityReading r = new AirQualityReading(UUID.randomUUID().toString(), ts, pm25);
            samples.add(r);
        }

        repository.saveAll(samples);
        logger.info("Inserted {} sample AirQualityReading records.", samples.size());
    }
}

