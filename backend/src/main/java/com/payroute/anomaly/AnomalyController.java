package com.payroute.anomaly;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/anomalies — returns the 100 most recent flagged transactions.
 * Consumed by the frontend anomaly log panel.
 */
@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyRepository anomalyRepository;

    @GetMapping
    public ResponseEntity<List<Anomaly>> getAnomalies() {
        return ResponseEntity.ok(anomalyRepository.findTop100ByOrderByDetectedAtDesc());
    }
}
