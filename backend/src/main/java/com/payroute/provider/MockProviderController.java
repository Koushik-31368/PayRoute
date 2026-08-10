package com.payroute.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Mock provider endpoints — simulate three payment gateways.
 *
 * These live in the same Spring Boot app as the orchestrator (not separate
 * services). The tradeoffs:
 *
 *   SAME APP (chosen here):
 *   ✓ One docker-compose service, simpler networking, faster startup
 *   ✓ Failure simulation is still realistic because ProviderSimulator adds
 *     real Thread.sleep() latency and random outcomes
 *   ✗ Failures here can't crash independently of the orchestrator
 *   ✗ In a real system, providers are external — you'd stub at the HTTP level
 *
 *   SEPARATE SERVICES (production approach):
 *   ✓ More realistic — network timeouts are actual TCP timeouts
 *   ✓ You can kill/restart provider containers independently
 *   ✗ 3 extra services in docker-compose, harder to manage in a demo
 *
 * For a portfolio project where the learning goal is the orchestration logic,
 * same-app simulation gives the same educational value with far less friction.
 *
 * In interviews: explain you made this deliberate simplification and know
 * how you'd extract them (separate Spring Boot apps / Wiremock stubs).
 */
@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
public class MockProviderController {

    private final ProviderSimulator simulator;

    /**
     * POST /api/mock/provider-a
     * Simulates Provider A: low failure rate (10%), low latency
     */
    @PostMapping("/provider-a")
    public Map<String, Object> callProviderA(@RequestBody Map<String, Object> payload)
            throws InterruptedException {
        return processRequest("PROVIDER_A", payload);
    }

    /**
     * POST /api/mock/provider-b
     * Simulates Provider B: medium failure rate (25%), medium latency
     */
    @PostMapping("/provider-b")
    public Map<String, Object> callProviderB(@RequestBody Map<String, Object> payload)
            throws InterruptedException {
        return processRequest("PROVIDER_B", payload);
    }

    /**
     * POST /api/mock/provider-c
     * Simulates Provider C: high failure rate (40%), high latency
     */
    @PostMapping("/provider-c")
    public Map<String, Object> callProviderC(@RequestBody Map<String, Object> payload)
            throws InterruptedException {
        return processRequest("PROVIDER_C", payload);
    }

    private Map<String, Object> processRequest(String provider, Map<String, Object> payload)
            throws InterruptedException {
        String txnId = (String) payload.getOrDefault("txnId", "unknown");
        BigDecimal amount = new BigDecimal(payload.getOrDefault("amount", "0").toString());

        ProviderResult result = simulator.simulate(provider, amount, txnId);

        return Map.of(
                "provider", provider,
                "txnId", txnId,
                "outcome", result.outcome().name(),
                "latencyMs", result.latencyMs(),
                "errorMessage", result.errorMessage() != null ? result.errorMessage() : ""
        );
    }
}
