package com.example.service;

import com.example.dto.ActionDto;
import com.example.dto.ForecastResponseDto;
import com.example.dto.HealthAdviceDto;
import com.example.dto.PredictionDto;
import com.example.exception.ExternalProcessTimeoutException;
import com.example.exception.ModelExecutionException;
import com.example.exception.NoDataException;
import com.example.model.AirQualityReading;
import com.example.repository.AirQualityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ForecastService {

    private final AirQualityRepository repository;
    private final String pythonExe;
    private final String bridgeScriptPath;
    private final long processTimeoutSeconds = 15;

    public ForecastService(AirQualityRepository repository,
                           @Value("${layhtu.python.exe:C:/Users/Hello/AppData/Local/Programs/Python/Python312/python.exe}") String pythonExe,
                           @Value("${layhtu.python.bridge:src/main/resources/Python/api_bridge.py}") String bridgeScriptPath) {
        this.repository = repository;
        this.pythonExe = pythonExe;
        this.bridgeScriptPath = bridgeScriptPath;
    }

    public ForecastResponseDto generateForecast() {
        // Fetch last 24 readings
        List<AirQualityReading> all = repository.findAllByOrderByTimestampDesc();
        if (all == null || all.size() < 24) {
            throw new NoDataException("Not enough readings to generate forecast (need 24)");
        }
        List<AirQualityReading> last24 = all.stream().limit(24).collect(Collectors.toList());
        // currently ordered newest->oldest; reverse to chronological
        Collections.reverse(last24);

        // Build CSV string
        String csv = last24.stream()
                .map(r -> r.getPm25() == null ? "0" : r.getPm25().toString())
                .collect(Collectors.joining(","));

        // Call Python bridge
        ProcessBuilder pb = new ProcessBuilder(pythonExe, bridgeScriptPath, csv);
        pb.environment().put("TF_CPP_MIN_LOG_LEVEL", "2"); // suppress TF INFO logs
        pb.redirectErrorStream(false); // keep stderr separate

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new ModelExecutionException("Failed to start Python process", e);
        }

        // Capture stderr asynchronously so TF logs don't mix with stdout
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<String> stderrFuture = executor.submit(() -> readStream(process.getErrorStream()));
        Future<String> stdoutFuture = executor.submit(() -> readStream(process.getInputStream()));

        boolean finished;
        try {
            finished = process.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new ExternalProcessTimeoutException("Python process interrupted", e);
        }

        if (!finished) {
            process.destroyForcibly();
            throw new ExternalProcessTimeoutException("Python bridge timed out after " + processTimeoutSeconds + "s");
        }

        int exit = process.exitValue();
        String stdout;
        String stderr;
        try {
            stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            stdout = "";
        }
        try {
            stderr = stderrFuture.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            stderr = "";
        }

        // Log stderr somewhere; for now keep it in local variable (could be logged)
        if (stderr != null && !stderr.isBlank()) {
            // avoid throwing, just record; in real app use logger
            System.err.println("[python-stderr] " + stderr.trim());
        }

        if (exit != 0) {
            throw new ModelExecutionException("Python bridge exited with code " + exit + ". Stdout: " + stdout + " Stderr: " + stderr);
        }

        // Parse stdout - api_bridge.py prints a single float
        String outTrim = stdout == null ? "" : stdout.trim();
        double predictionValue;
        try {
            predictionValue = Double.parseDouble(outTrim.split("\n")[0].trim());
        } catch (Exception e) {
            throw new ModelExecutionException("Failed to parse model output: '" + outTrim + "'", e);
        }

        // Prepare DTOs: map observed and predicted for final timestamp; carousel expects list of predictions
        List<PredictionDto> preds = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

        // For simplicity, create 1 prediction for the next hour using last timestamp
        AirQualityReading last = last24.get(last24.size() - 1);
        String time = fmt.format(last.getTimestamp());
        Double observed = last.getPm25();
        Double predicted = predictionValue;
        Double confidence = 0.85; // placeholder - real model may return confidence
        String label = labelFromPm25(predicted);
        String iconUrl = iconForLabel(label);
        preds.add(new PredictionDto(time, observed, predicted, confidence, label, iconUrl));

        // Create simple actions and advice based on label
        List<ActionDto> actions = new ArrayList<>();
        actions.add(new ActionDto("Reduce outdoor activity", "Limit time outside if PM2.5 is high."));

        List<HealthAdviceDto> advice = new ArrayList<>();
        advice.add(new HealthAdviceDto(label, adviceForLabel(label)));

        // Shutdown executor
        executor.shutdownNow();

        return new ForecastResponseDto(preds, actions, advice);
    }

    private String readStream(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }

    private String labelFromPm25(Double pm25) {
        if (pm25 == null) return "Unknown";
        if (pm25 <= 12) return "Good";
        if (pm25 <= 35.4) return "Moderate";
        if (pm25 <= 55.4) return "Unhealthy for Sensitive Groups";
        if (pm25 <= 150.4) return "Unhealthy";
        return "Very Unhealthy";
    }

    private String iconForLabel(String label) {
        return "/icons/air_" + label.toLowerCase().replaceAll("\\s+", "_") + ".png";
    }

    private String adviceForLabel(String label) {
        switch (label) {
            case "Good": return "Air quality is good. No precautions necessary.";
            case "Moderate": return "Consider reducing prolonged outdoor exertion.";
            case "Unhealthy for Sensitive Groups": return "People with respiratory disease should limit outdoor exertion.";
            case "Unhealthy": return "Reduce outdoor activity and consider wearing a mask.";
            case "Very Unhealthy": return "Avoid outdoor activity and seek indoor air filtration.";
            default: return "No advice available.";
        }
    }
}

