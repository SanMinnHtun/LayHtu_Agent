package com.example.service;

import com.example.model.AirQualityReading;
import com.example.repository.AirQualityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityService.class);

    private final AirQualityRepository repository;
    private final String pythonExe;
    private final String bridgeScriptPath;
    private final long processTimeoutSeconds = 20;

    public AirQualityService(AirQualityRepository repository,
                             @Value("${layhtu.python.exe:C:/Users/Hello/AppData/Local/Programs/Python/Python312/python.exe}") String pythonExe,
                             @Value("${layhtu.python.bridge:src/main/resources/Python/api_bridge.py}") String bridgeScriptPath) {
        this.repository = repository;
        this.pythonExe = pythonExe;
        this.bridgeScriptPath = bridgeScriptPath;
    }

    /**
     * Fetches the latest 24 PM2.5 values, formats them as a space-separated string,
     * calls the Python bridge, and returns the predicted value as Optional<Double>.
     * Returns Optional.empty() on error (not enough data, python failure, parse error).
     */
    public Optional<Double> predictFromLast24() {
        try {
            List<AirQualityReading> latest = repository.findTop24ByOrderByTimestampDesc();
            if (latest == null || latest.size() < 24) {
                logger.warn("Not enough readings to predict - need 24 samples");
                return Optional.empty();
            }

            // repository returns newest-first; reverse to chronological (oldest..newest)
            List<AirQualityReading> ordered = new ArrayList<>(latest);
            java.util.Collections.reverse(ordered);

            // Build space-separated string of pm25 values, replacing nulls with 0
            String payload = ordered.stream()
                    .map(r -> r.getPm25() == null ? "0" : r.getPm25().toString())
                    .collect(Collectors.joining(" "));

            ProcessBuilder pb = new ProcessBuilder(pythonExe, bridgeScriptPath, payload);
            // suppress TF info logs if script uses TensorFlow
            pb.environment().put("TF_CPP_MIN_LOG_LEVEL", "2");
            pb.redirectErrorStream(false);

            Process process;
            try {
                process = pb.start();
            } catch (IOException e) {
                logger.error("Failed to start Python process", e);
                return Optional.empty();
            }

            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<String> stdoutF = executor.submit(() -> readStream(process.getInputStream()));
                Future<String> stderrF = executor.submit(() -> readStream(process.getErrorStream()));

                boolean finished;
                try {
                    finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                    Thread.currentThread().interrupt();
                    logger.error("Python process interrupted", e);
                    return Optional.empty();
                }

                if (!finished) {
                    process.destroyForcibly();
                    logger.error("Python bridge timed out after {}s", processTimeoutSeconds);
                    return Optional.empty();
                }

                int exit = process.exitValue();
                String stdout = "";
                String stderr = "";
                try {
                    stdout = stdoutF.get(1, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
                try {
                    stderr = stderrF.get(1, TimeUnit.SECONDS);
                } catch (Exception ignored) {}

                if (stderr != null && !stderr.isBlank()) {
                    logger.warn("[python-stderr] {}", stderr.trim());
                }

                if (exit != 0) {
                    logger.error("Python bridge exited with code {}. Stderr: {}", exit, stderr);
                    return Optional.empty();
                }

                String outTrim = stdout == null ? "" : stdout.trim();
                try {
                    String firstLine = outTrim.split("\\n")[0].trim();
                    Double value = Double.parseDouble(firstLine);
                    logger.info("Python predicted value: {}", value);
                    return Optional.of(value);
                } catch (Exception e) {
                    logger.error("Failed to parse Python output: '{}'", outTrim, e);
                    return Optional.empty();
                }
            } finally {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            logger.error("Unexpected error in predictFromLast24", e);
            return Optional.empty();
        }
    }

    private String readStream(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.warn("Failed to read process stream", e);
            return "";
        }
    }
}
