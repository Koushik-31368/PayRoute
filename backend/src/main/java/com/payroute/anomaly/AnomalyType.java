package com.payroute.anomaly;

/**
 * The three anomaly rules currently implemented.
 *
 * LARGE_AMOUNT        — Transaction amount exceeds the configured threshold.
 *                       Rationale: unusually large payments may indicate fraud
 *                       or a misconfigured integration. Configurable so you can
 *                       tune it per business context (retail vs. B2B).
 *
 * BURST_FROM_SOURCE   — The same source has submitted more than N transactions
 *                       within a short time window (e.g. 5 txns in 60 seconds).
 *                       Rationale: could indicate a bot, a runaway retry loop,
 *                       or a card testing attack.
 *
 * REPEATED_FAILURES   — The same source has had multiple recent failed
 *                       transactions. Rationale: a source that keeps failing
 *                       may be using bad card details, be rate-limited, or be
 *                       attempting to probe the system.
 */
public enum AnomalyType {
    LARGE_AMOUNT,
    BURST_FROM_SOURCE,
    REPEATED_FAILURES
}
