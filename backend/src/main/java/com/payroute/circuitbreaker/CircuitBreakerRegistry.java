package com.payroute.circuitbreaker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds one {@link CircuitBreaker} instance per provider.
 *
 * Why a registry (not individual @Beans)?
 *   The number of providers is dynamic from config, and we need to iterate
 *   all of them in the routing engine. A Map keyed by provider name is
 *   the cleanest pattern here — same approach used by Resilience4j internals.
 *
 * This is a @Component singleton. It reads provider names and circuit breaker
 * settings from application.properties at startup.
 */
@Component
public class CircuitBreakerRegistry {

    /** Provider names, e.g. ["PROVIDER_A", "PROVIDER_B", "PROVIDER_C"] */
    @Value("${payroute.providers}")
    private List<String> providerNames;

    @Value("${payroute.circuit-breaker.window-size:20}")
    private int windowSize;

    @Value("${payroute.circuit-breaker.failure-threshold:0.50}")
    private double failureThreshold;

    @Value("${payroute.circuit-breaker.cooldown-seconds:30}")
    private long cooldownSeconds;

    /** Lazily initialised map — populated on first access to getBreaker(). */
    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    /**
     * Returns the CircuitBreaker for the given provider, creating it on first
     * access. computeIfAbsent is atomic (ConcurrentHashMap guarantees it), so
     * there's no race condition between two threads calling this simultaneously.
     */
    public CircuitBreaker getBreaker(String providerName) {
        return breakers.computeIfAbsent(providerName, name ->
                new CircuitBreaker(name, windowSize, failureThreshold, cooldownSeconds));
    }

    /** Returns all registered circuit breakers (for the health endpoint). */
    public Collection<CircuitBreaker> getAllBreakers() {
        // Ensure all providers are initialised before returning
        providerNames.forEach(this::getBreaker);
        return breakers.values();
    }

    /** Ordered list of providers — used by the routing engine. */
    public List<String> getProviderNames() {
        return providerNames;
    }
}
