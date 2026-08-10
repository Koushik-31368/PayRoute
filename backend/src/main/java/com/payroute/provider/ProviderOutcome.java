package com.payroute.provider;

/**
 * The outcome of a single simulated provider call.
 * Distinct from {@link com.payroute.transaction.AttemptResult} — that's the
 * domain-level enum; this is the provider layer's raw response before it's
 * mapped into a TransactionAttempt.
 */
public enum ProviderOutcome {
    SUCCESS,
    FAILURE,
    TIMEOUT
}
