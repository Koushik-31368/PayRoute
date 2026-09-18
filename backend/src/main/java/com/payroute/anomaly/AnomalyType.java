package com.payroute.anomaly;

/**
 * The three anomaly rules currently implemented.
 *
 * <ul>
 *   <li><b>LARGE_AMOUNT</b> – Transaction amount exceeds the configured threshold.
 *       Rationale: unusually large payments may indicate fraud or a misconfigured
 *       integration. Configurable so you can tune it per business context (retail vs. B2B).</li>
 *
 *   <li><b>BURST_FROM_SOURCE</b> – The same source has submitted more than N transactions
 *       within a short time window (e.g. 5 txns in 60 seconds).
 *       Rationale: could indicate a bot, a runaway retry loop, or a card testing attack.</li>
 *
 *   <li><b>REPEATED_FAILURES</b> – The same source has had multiple recent failed
 *       transactions. Rationale: a source that keeps failing may be using bad card details,
 *       be rate-limited, or be attempting to probe the system.</li>
 * </ul>
 *
 * Adding a new rule: add an enum constant here, implement the check in
 * {@link AnomalyDetectionService#checkAndFlag}, and update the frontend TYPE_META map.
 */
/** @since 1.0.0 */
public enum AnomalyType {
    LARGE_AMOUNT,
    BURST_FROM_SOURCE,
    REPEATED_FAILURES
}

