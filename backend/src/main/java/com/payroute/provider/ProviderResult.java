package com.payroute.provider;

/**
 * Value object returned by {@link ProviderSimulator#simulate}.
 * Carries the raw outcome, actual latency measured, and any error message.
 *
 * Using a record here — it's immutable, auto-generates equals/hashCode/toString,
 * and signals clearly that this is pure data with no behaviour.
 */
public record ProviderResult(
        ProviderOutcome outcome,
        long latencyMs,
        String errorMessage
) {}
