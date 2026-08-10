package com.payroute.transaction;

import com.payroute.anomaly.Anomaly;
import com.payroute.anomaly.AnomalyDetectionService;
import com.payroute.circuitbreaker.CircuitBreaker;
import com.payroute.circuitbreaker.CircuitBreakerRegistry;
import com.payroute.provider.ProviderOutcome;
import com.payroute.provider.ProviderResult;
import com.payroute.provider.ProviderSimulator;
import com.payroute.websocket.TransactionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The orchestration heart of PayRoute.
 *
 * This service handles the full lifecycle of a payment request:
 *   1. Idempotency check (deduplicate retries)
 *   2. Create a PENDING Transaction record
 *   3. Route through providers, respecting circuit breaker state
 *   4. On failure, failover to the next available provider
 *   5. Persist every attempt for audit + circuit breaker input
 *   6. Run anomaly detection
 *   7. Broadcast the result via WebSocket
 *
 * ── Routing Strategy ─────────────────────────────────────────────────────────
 * We iterate providers in order of ascending failure rate (best first).
 * This is a simple but effective strategy. In a real system you might use:
 *   - Weighted random (send proportionally more traffic to healthy providers)
 *   - Least-latency routing (prefer the fastest provider recently)
 *   - Cost-based routing (prefer cheaper providers first, fallback to premium)
 *
 * The key insight: we DON'T use pure round-robin because a round-robin that
 * hits a failing provider 33% of the time is 33% wasteful. Success-rate-based
 * routing continuously self-optimises.
 *
 * ── Idempotency Implementation ───────────────────────────────────────────────
 * The idempotency key is stored with a UNIQUE DB constraint. If two concurrent
 * requests arrive with the same key (e.g., client double-clicked "Pay"):
 *   - The first request inserts the Transaction row (succeeds).
 *   - The second request checks findByIdempotencyKey() before inserting,
 *     sees the existing row, and returns it immediately.
 *
 * This handles the HTTP-retry case cleanly. However, there's a subtle race
 * condition: two concurrent requests could both pass the initial
 * findByIdempotencyKey() check (both see null) and then both try to insert.
 * The DB UNIQUE constraint makes one of them throw a DataIntegrityViolationException.
 * For this project we don't catch that — if you need full correctness,
 * add a distributed lock (Redis SETNX) or database advisory lock around
 * the check-and-insert.
 *
 * ── @Transactional boundary ───────────────────────────────────────────────────
 * The entire process runs in one @Transactional context. This means:
 *   - If the DB fails to persist a TransactionAttempt, the whole transaction
 *     rolls back — we never record partial state.
 *   - Anomaly detection runs in a REQUIRES_NEW transaction (see AnomalyDetectionService)
 *     so its failure never rolls back the main payment record.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final TransactionAttemptRepository attemptRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ProviderSimulator providerSimulator;
    private final AnomalyDetectionService anomalyDetectionService;
    private final TransactionEventPublisher eventPublisher;

    @Transactional
    public Transaction process(TransactionRequest request) {
        // ── Step 1: Idempotency check ──────────────────────────────────────
        String idempotencyKey = (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank())
                ? request.getIdempotencyKey()
                : UUID.randomUUID().toString();

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("[Idempotency] Key {} already processed, returning existing result: {}",
                    idempotencyKey, existing.get().getStatus());
            return existing.get();
        }

        // ── Step 2: Create PENDING transaction record ─────────────────────
        Transaction txn = new Transaction();
        txn.setIdempotencyKey(idempotencyKey);
        txn.setAmount(request.getAmount());
        txn.setSource(request.getSource());
        txn.setStatus(TransactionStatus.PENDING);
        txn = transactionRepository.save(txn);

        // ── Step 3: Route through providers ───────────────────────────────
        // Sort providers: try the ones with the lowest current failure rate first.
        List<String> orderedProviders = circuitBreakerRegistry.getProviderNames().stream()
                .sorted((a, b) -> Double.compare(
                        circuitBreakerRegistry.getBreaker(a).getFailureRate(),
                        circuitBreakerRegistry.getBreaker(b).getFailureRate()))
                .toList();

        int attemptNumber = 0;
        boolean succeeded = false;

        for (String provider : orderedProviders) {
            CircuitBreaker cb = circuitBreakerRegistry.getBreaker(provider);
            attemptNumber++;

            // ── Circuit breaker gate ───────────────────────────────────────
            if (!cb.allowRequest()) {
                log.info("[Router] {} circuit breaker is OPEN — skipping", provider);
                TransactionAttempt skipped = buildAttempt(txn, provider, AttemptResult.SKIPPED, 0, attemptNumber, "Circuit breaker OPEN");
                txn.getAttempts().add(skipped);
                attemptRepository.save(skipped);
                continue;
            }

            // ── Call the provider (simulated) ─────────────────────────────
            long startMs = System.currentTimeMillis();
            ProviderResult result;
            try {
                result = providerSimulator.simulate(provider, txn.getAmount(), txn.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result = new ProviderResult(ProviderOutcome.TIMEOUT, System.currentTimeMillis() - startMs, "Interrupted");
            }

            // ── Map provider outcome to domain result ──────────────────────
            AttemptResult attemptResult = switch (result.outcome()) {
                case SUCCESS -> AttemptResult.SUCCESS;
                case FAILURE -> AttemptResult.FAILURE;
                case TIMEOUT -> AttemptResult.TIMEOUT;
            };

            // ── Record the attempt ─────────────────────────────────────────
            TransactionAttempt attempt = buildAttempt(
                    txn, provider, attemptResult, result.latencyMs(),
                    attemptNumber, result.errorMessage());
            txn.getAttempts().add(attempt);
            attemptRepository.save(attempt);

            // ── Inform the circuit breaker ─────────────────────────────────
            cb.recordResult(attemptResult);

            if (attemptResult == AttemptResult.SUCCESS) {
                txn.setFinalProvider(provider);
                succeeded = true;
                log.info("[Router] {} succeeded on {} after {} attempt(s)",
                        txn.getId(), provider, attemptNumber);
                break;
            } else {
                log.warn("[Router] {} failed on {} ({}), trying next provider",
                        txn.getId(), provider, attemptResult);
            }
        }

        // ── Step 4: Finalise transaction status ────────────────────────────
        txn.setStatus(succeeded ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        txn.setCompletedAt(Instant.now());
        txn = transactionRepository.save(txn);

        // ── Step 5: Anomaly detection ──────────────────────────────────────
        // Runs in its own transaction (REQUIRES_NEW) — won't affect main commit.
        List<Anomaly> anomalies;
        try {
            anomalies = anomalyDetectionService.checkAndFlag(txn);
        } catch (Exception e) {
            log.error("[Anomaly] Detection failed for txn {}: {}", txn.getId(), e.getMessage());
            anomalies = List.of();
        }

        // ── Step 6: Broadcast via WebSocket ───────────────────────────────
        // Push the event after the transaction is fully committed.
        // Note: broadcasting inside a @Transactional method means the WS event
        // is sent before the commit flushes. For strict consistency, use
        // @TransactionalEventListener(phase = AFTER_COMMIT) instead.
        final Transaction finalTxn = txn;
        eventPublisher.publishTransactionComplete(finalTxn);
        anomalies.forEach(eventPublisher::publishAnomaly);

        return txn;
    }

    private TransactionAttempt buildAttempt(Transaction txn, String provider,
                                             AttemptResult result, long latencyMs,
                                             int attemptNumber, String errorMessage) {
        TransactionAttempt attempt = new TransactionAttempt();
        attempt.setTransaction(txn);
        attempt.setProvider(provider);
        attempt.setResult(result);
        attempt.setLatencyMs(latencyMs);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setErrorMessage(errorMessage != null && errorMessage.length() > 500
                ? errorMessage.substring(0, 500) : errorMessage);
        return attempt;
    }
}
