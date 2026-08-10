package com.payroute.circuitbreaker;

import com.payroute.transaction.AttemptResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manual circuit breaker implementation for a single provider.
 *
 * ── Rolling Window ────────────────────────────────────────────────────────────
 * We track the last N attempt results in a Deque (double-ended queue). When a
 * new result arrives, it's added to the tail; if the deque exceeds capacity,
 * the oldest result is removed from the head. This gives us an O(1) sliding
 * window without any scheduled cleanup jobs.
 *
 * ── Thread Safety ─────────────────────────────────────────────────────────────
 * State transitions must be atomic. A naive synchronized(this) on every method
 * would work, but we use a ReentrantLock so the HALF_OPEN probe logic (allow
 * exactly one request through) can hold the lock across the check-and-set
 * without risk of two concurrent requests both thinking they're the probe.
 *
 * ── Why not Resilience4j? ─────────────────────────────────────────────────────
 * Resilience4j is excellent in production, but it hides exactly the state
 * machine logic you need to understand. This implementation makes every
 * transition explicit and inspectable. Once you understand this, Resilience4j
 * configuration will be obvious.
 *
 * ── HALF_OPEN probe semantics ─────────────────────────────────────────────────
 * When state is HALF_OPEN, {@link #allowRequest()} returns true exactly once
 * (for the probe request), then returns false for all subsequent calls until
 * the probe result is recorded. The {@code probeInFlight} flag enforces this.
 */
public class CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    // ── Configuration (injected at construction, read from application.properties) ──

    /** Provider identifier, e.g. "PROVIDER_A". */
    @Getter private final String providerName;

    /** Maximum size of the rolling window (e.g. 20 attempts). */
    private final int windowSize;

    /**
     * Failure rate above which the breaker opens (e.g. 0.50 = 50%).
     * Only evaluated when the window has at least {@code windowSize} samples.
     */
    private final double failureThreshold;

    /**
     * How long to stay OPEN before transitioning to HALF_OPEN (in seconds).
     * After this period, one probe request is allowed through.
     */
    private final long cooldownSeconds;

    // ── Mutable State (protected by lock) ─────────────────────────────────────

    /** Current state of the breaker. AtomicReference for visibility. */
    private final AtomicReference<CircuitBreakerState> state =
            new AtomicReference<>(CircuitBreakerState.CLOSED);

    /**
     * Rolling window of recent attempt results.
     * true  = the attempt was a failure (FAILURE or TIMEOUT)
     * false = the attempt was a success
     */
    private final Deque<Boolean> window = new ArrayDeque<>();

    /** Count of failures currently in the window (maintained incrementally). */
    private int failureCount = 0;

    /** When the breaker was opened. Used to compute cooldown expiry. */
    private Instant openedAt;

    /**
     * When in HALF_OPEN: true if a probe is already in-flight.
     * Ensures only one probe request goes through at a time.
     */
    private boolean probeInFlight = false;

    /** Protects all mutable state. */
    private final ReentrantLock lock = new ReentrantLock();

    // ── Constructor ───────────────────────────────────────────────────────────

    public CircuitBreaker(String providerName, int windowSize,
                          double failureThreshold, long cooldownSeconds) {
        this.providerName = providerName;
        this.windowSize = windowSize;
        this.failureThreshold = failureThreshold;
        this.cooldownSeconds = cooldownSeconds;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns true if a request to this provider should be allowed.
     *
     * CLOSED    → always allow.
     * OPEN      → deny, unless the cooldown has expired, in which case
     *             transition to HALF_OPEN and allow the first probe.
     * HALF_OPEN → allow only if no probe is already in-flight.
     */
    public boolean allowRequest() {
        lock.lock();
        try {
            return switch (state.get()) {
                case CLOSED -> true;

                case OPEN -> {
                    if (isCooldownExpired()) {
                        transitionTo(CircuitBreakerState.HALF_OPEN);
                        probeInFlight = true;
                        yield true;  // This caller is the probe
                    }
                    yield false;
                }

                case HALF_OPEN -> {
                    if (!probeInFlight) {
                        probeInFlight = true;
                        yield true;
                    }
                    yield false; // Probe already in-flight, reject other requests
                }
            };
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records the result of an attempt and updates the rolling window.
     *
     * This is called AFTER the provider responds (or times out). It drives
     * all state transitions:
     *
     *   CLOSED    + failure → update window, check threshold, maybe → OPEN
     *   CLOSED    + success → update window (good signal)
     *   HALF_OPEN + success → clear window, → CLOSED  (provider recovered!)
     *   HALF_OPEN + failure → → OPEN (reset cooldown, provider still sick)
     *   OPEN                → should not happen (requests are rejected), ignore
     */
    public void recordResult(AttemptResult result) {
        lock.lock();
        try {
            boolean isFailure = (result == AttemptResult.FAILURE || result == AttemptResult.TIMEOUT);

            CircuitBreakerState currentState = state.get();

            if (currentState == CircuitBreakerState.HALF_OPEN) {
                probeInFlight = false; // Probe has returned, clear the flag
                if (!isFailure) {
                    // Provider responded successfully — it has recovered.
                    // Reset the window (stale failures from the sick period
                    // shouldn't penalise the now-healthy provider).
                    window.clear();
                    failureCount = 0;
                    transitionTo(CircuitBreakerState.CLOSED);
                } else {
                    // Provider is still unhealthy — go back to OPEN and restart cooldown.
                    transitionTo(CircuitBreakerState.OPEN);
                }
                return;
            }

            if (currentState == CircuitBreakerState.OPEN) {
                // Shouldn't receive results here (allowRequest returns false),
                // but be defensive.
                return;
            }

            // State is CLOSED — update the rolling window.
            addToWindow(isFailure);

            // Only evaluate the threshold once we have a full window.
            // Rationale: opening the breaker on 3 failures out of 3 attempts
            // (100% rate) when we've only just started would be a false positive.
            if (window.size() >= windowSize) {
                double failureRate = (double) failureCount / window.size();
                if (failureRate >= failureThreshold) {
                    log.warn("[CircuitBreaker] {} failure rate {:.0f}% >= threshold {:.0f}% — OPENING",
                            providerName, String.format("%.0f", failureRate * 100),
                            String.format("%.0f", failureThreshold * 100));
                    transitionTo(CircuitBreakerState.OPEN);
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /** Returns the current state without acquiring the lock (safe for reading). */
    public CircuitBreakerState getState() {
        return state.get();
    }

    /**
     * Returns the current failure rate as a value in [0.0, 1.0].
     * Returns 0.0 if the window is empty.
     */
    public double getFailureRate() {
        lock.lock();
        try {
            return window.isEmpty() ? 0.0 : (double) failureCount / window.size();
        } finally {
            lock.unlock();
        }
    }

    /** Returns how many samples are currently in the rolling window. */
    public int getWindowSampleCount() {
        lock.lock();
        try {
            return window.size();
        } finally {
            lock.unlock();
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Adds a result to the rolling window, evicting the oldest entry if
     * the window is at capacity.
     *
     * Maintaining failureCount incrementally (rather than re-counting on
     * every read) keeps recordResult() O(1) instead of O(N).
     */
    private void addToWindow(boolean isFailure) {
        if (window.size() >= windowSize) {
            boolean evicted = window.pollFirst(); // Remove oldest
            if (evicted) failureCount--;          // Decrement if it was a failure
        }
        window.addLast(isFailure);
        if (isFailure) failureCount++;
    }

    private boolean isCooldownExpired() {
        return openedAt != null &&
               Instant.now().isAfter(openedAt.plusSeconds(cooldownSeconds));
    }

    private void transitionTo(CircuitBreakerState newState) {
        CircuitBreakerState prev = state.getAndSet(newState);
        if (newState == CircuitBreakerState.OPEN) {
            openedAt = Instant.now();
        }
        log.info("[CircuitBreaker] {} transitioned: {} → {}", providerName, prev, newState);
    }
}
