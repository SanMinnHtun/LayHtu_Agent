package com.example.repository;

import com.example.model.AirQualityReading;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirQualityRepository extends MongoRepository<AirQualityReading, String> {
    // Find the most recent readings ordered by timestamp descending; callers may limit to 24
    List<AirQualityReading> findAllByOrderByTimestampDesc();
}

