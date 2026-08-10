package com.payroute.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates provider behaviour: artificial latency + random outcome.
 *
 * Each provider has a configured failure rate (probability of FAILURE),
 * a timeout rate (probability of TIMEOUT), and a latency range (min–max ms).
 *
 * Design note:
 *   In a real system, calling Thread.sleep() on a request thread blocks the
 *   servlet thread pool. Here it's acceptable because this is a simulation
 *   and the total concurrency is low. In production, you'd use async HTTP
 *   clients (WebClient, Feign + Reactor) and non-blocking I/O.
 */
@Component
public class ProviderSimulator {

    // Failure rates loaded from application.properties
    @Value("${payroute.providers.PROVIDER_A.failure-rate:0.10}")
    private double providerAFailureRate;

    @Value("${payroute.providers.PROVIDER_B.failure-rate:0.25}")
    private double providerBFailureRate;

    @Value("${payroute.providers.PROVIDER_C.failure-rate:0.40}")
    private double providerCFailureRate;

    // Timeout rates (separate from failure — provider is slow, not wrong)
    @Value("${payroute.providers.PROVIDER_A.timeout-rate:0.05}")
    private double providerATimeoutRate;

    @Value("${payroute.providers.PROVIDER_B.timeout-rate:0.10}")
    private double providerBTimeoutRate;

    @Value("${payroute.providers.PROVIDER_C.timeout-rate:0.15}")
    private double providerCTimeoutRate;

    // Latency range (ms) — simulates real network variance
    @Value("${payroute.providers.PROVIDER_A.min-latency-ms:50}")
    private int providerAMinLatency;

    @Value("${payroute.providers.PROVIDER_A.max-latency-ms:200}")
    private int providerAMaxLatency;

    @Value("${payroute.providers.PROVIDER_B.min-latency-ms:100}")
    private int providerBMinLatency;

    @Value("${payroute.providers.PROVIDER_B.max-latency-ms:400}")
    private int providerBMaxLatency;

    @Value("${payroute.providers.PROVIDER_C.min-latency-ms:200}")
    private int providerCMinLatency;

    @Value("${payroute.providers.PROVIDER_C.max-latency-ms:800}")
    private int providerCMaxLatency;

    private final Random random = new Random();

    /**
     * Simulates calling a provider, including artificial latency.
     *
     * @param provider  Provider identifier ("PROVIDER_A", etc.)
     * @param amount    Payment amount (not used in simulation, but logged for realism)
     * @param txnId     Transaction ID (same)
     * @return          {@link ProviderResult} containing outcome and actual latency
     */
    public ProviderResult simulate(String provider, BigDecimal amount, String txnId)
            throws InterruptedException {

        ProviderConfig cfg = getConfig(provider);

        // Simulate network latency
        long latency = cfg.minLatency + (long)(random.nextDouble() * (cfg.maxLatency - cfg.minLatency));
        Thread.sleep(latency);

        // Determine outcome based on configured probabilities
        double roll = random.nextDouble();

        if (roll < cfg.timeoutRate) {
            return new ProviderResult(ProviderOutcome.TIMEOUT, latency,
                    "Provider timed out after " + latency + "ms");
        } else if (roll < cfg.timeoutRate + cfg.failureRate) {
            return new ProviderResult(ProviderOutcome.FAILURE, latency,
                    "Provider declined: insufficient funds (simulated)");
        } else {
            return new ProviderResult(ProviderOutcome.SUCCESS, latency, null);
        }
    }

    private ProviderConfig getConfig(String provider) {
        return switch (provider) {
            case "PROVIDER_A" -> new ProviderConfig(
                    providerAFailureRate, providerATimeoutRate,
                    providerAMinLatency, providerAMaxLatency);
            case "PROVIDER_B" -> new ProviderConfig(
                    providerBFailureRate, providerBTimeoutRate,
                    providerBMinLatency, providerBMaxLatency);
            case "PROVIDER_C" -> new ProviderConfig(
                    providerCFailureRate, providerCTimeoutRate,
                    providerCMinLatency, providerCMaxLatency);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }

    /** Value object grouping simulation parameters for one provider. */
    private record ProviderConfig(double failureRate, double timeoutRate,
                                   int minLatency, int maxLatency) {}
}
