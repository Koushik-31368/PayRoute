package com.payroute.circuitbreaker;

/**
 * The three states of a circuit breaker, modelled as a simple enum.
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                    CIRCUIT BREAKER STATES                        │
 * │                                                                  │
 * │  CLOSED ──[failure rate > threshold]──► OPEN                    │
 * │    ▲                                      │                      │
 * │    │                              [cooldown expires]             │
 * │    │                                      │                      │
 * │    └──[probe succeeds]────── HALF_OPEN ◄──┘                     │
 * │                               │                                  │
 * │                    [probe fails]──► OPEN (reset timer)           │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * CLOSED   — Normal operation. All requests flow through. The breaker
 *            tracks the rolling success/failure rate.
 *
 * OPEN     — The provider is considered unhealthy. All routing attempts
 *            to this provider are rejected immediately (recorded as SKIPPED)
 *            without making an actual network call. A cooldown timer starts.
 *
 * HALF_OPEN — The cooldown has expired. The breaker lets through exactly
 *             ONE "probe" request to test if the provider has recovered.
 *             If the probe succeeds → back to CLOSED.
 *             If the probe fails   → back to OPEN (cooldown resets).
 *
 * Why HALF_OPEN matters:
 *   Without it, you'd either stay OPEN forever (losing revenue) or snap
 *   back to CLOSED after cooldown and immediately hammer a still-sick
 *   provider with full traffic. HALF_OPEN is the graduated re-entry that
 *   makes circuit breakers production-safe.
 */
public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
