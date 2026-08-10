package com.payroute.transaction;

/**
 * Result of a single provider attempt.
 *
 * SUCCESS — provider accepted the payment.
 * FAILURE — provider explicitly rejected it (e.g. card declined).
 * TIMEOUT — provider did not respond within the configured timeout window.
 *           Treated as a failure for circuit breaker purposes, but kept
 *           distinct so you can analyse timeout patterns separately.
 * SKIPPED — provider was skipped because its circuit breaker was OPEN.
 *           Recorded so the audit trail shows the routing decision.
 */
public enum AttemptResult {
    SUCCESS,
    FAILURE,
    TIMEOUT,
    SKIPPED
}
