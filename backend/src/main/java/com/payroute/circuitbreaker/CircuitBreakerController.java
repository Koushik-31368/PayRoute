package com.payroute.circuitbreaker;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Exposes circuit breaker state for all providers.
 * Used by the frontend provider health cards.
 */
@RestController
@RequestMapping("/api/circuit-breakers")
@RequiredArgsConstructor
public class CircuitBreakerController {

    private final CircuitBreakerRegistry registry;

    /**
     * GET /api/circuit-breakers
     *
     * Returns a list of provider health summaries:
     * [
     *   { "provider": "PROVIDER_A", "state": "CLOSED", "failureRate": 0.05, "windowSamples": 20 },
     *   ...
     * ]
     */
    @GetMapping
    public List<Map<String, Object>> getCircuitBreakerStates() {
        return registry.getAllBreakers().stream()
                .map(cb -> Map.<String, Object>of(
                        "provider", cb.getProviderName(),
                        "state", cb.getState().name(),
                        "failureRate", Math.round(cb.getFailureRate() * 10000.0) / 100.0, // as %
                        "windowSamples", cb.getWindowSampleCount()
                ))
                .toList();
    }
}
