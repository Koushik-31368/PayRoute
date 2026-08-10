package com.payroute.anomaly;

import com.payroute.transaction.Transaction;
import com.payroute.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based anomaly detection — no ML, just configurable thresholds.
 *
 * Called AFTER a transaction is persisted (not before) so the transaction
 * itself is always recorded regardless of anomaly status.
 *
 * Three checks are run:
 *
 *  1. LARGE_AMOUNT     — amount > threshold (default: 100,000)
 *  2. BURST_FROM_SOURCE — source submitted > burstLimit txns in burstWindowSeconds
 *  3. REPEATED_FAILURES — source had > failureLimit failed txns in last failureWindowSeconds
 *
 * @Transactional(propagation = REQUIRES_NEW) — why?
 *   We want anomaly detection to succeed or fail independently of the main
 *   transaction commit. If anomaly detection throws (e.g. DB hiccup), we
 *   don't want to rollback the payment record. REQUIRES_NEW suspends the
 *   calling transaction and runs anomaly detection in its own transaction.
 */
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final AnomalyRepository anomalyRepository;
    private final TransactionRepository transactionRepository;

    @Value("${payroute.anomaly.large-amount-threshold:100000}")
    private BigDecimal largeAmountThreshold;

    @Value("${payroute.anomaly.burst-limit:5}")
    private long burstLimit;

    @Value("${payroute.anomaly.burst-window-seconds:60}")
    private long burstWindowSeconds;

    @Value("${payroute.anomaly.failure-limit:3}")
    private long failureLimit;

    @Value("${payroute.anomaly.failure-window-seconds:300}")
    private long failureWindowSeconds;

    /**
     * Run all anomaly checks for a completed transaction and persist any
     * violations found.
     *
     * @return list of Anomaly records created (may be empty)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Anomaly> checkAndFlag(Transaction transaction) {
        List<Anomaly> anomalies = new ArrayList<>();

        // Rule 1: Large amount
        if (transaction.getAmount().compareTo(largeAmountThreshold) > 0) {
            if (!alreadyFlagged(transaction.getId(), AnomalyType.LARGE_AMOUNT)) {
                anomalies.add(buildAnomaly(transaction, AnomalyType.LARGE_AMOUNT,
                        String.format("Amount %.2f exceeds threshold %.2f",
                                transaction.getAmount(), largeAmountThreshold)));
            }
        }

        // Rule 2: Burst from same source
        Instant burstWindowStart = Instant.now().minus(burstWindowSeconds, ChronoUnit.SECONDS);
        long recentCount = transactionRepository
                .countBySourceAndCreatedAtAfter(transaction.getSource(), burstWindowStart);
        if (recentCount > burstLimit) {
            if (!alreadyFlagged(transaction.getId(), AnomalyType.BURST_FROM_SOURCE)) {
                anomalies.add(buildAnomaly(transaction, AnomalyType.BURST_FROM_SOURCE,
                        String.format("Source '%s' submitted %d transactions in the last %ds (limit: %d)",
                                transaction.getSource(), recentCount, burstWindowSeconds, burstLimit)));
            }
        }

        // Rule 3: Repeated failures from same source
        Instant failureWindowStart = Instant.now().minus(failureWindowSeconds, ChronoUnit.SECONDS);
        List<?> recentFailures = transactionRepository
                .findRecentFailuresBySource(transaction.getSource(), failureWindowStart);
        if (recentFailures.size() >= failureLimit) {
            if (!alreadyFlagged(transaction.getId(), AnomalyType.REPEATED_FAILURES)) {
                anomalies.add(buildAnomaly(transaction, AnomalyType.REPEATED_FAILURES,
                        String.format("Source '%s' had %d failed transactions in the last %ds",
                                transaction.getSource(), recentFailures.size(), failureWindowSeconds)));
            }
        }

        return anomalyRepository.saveAll(anomalies);
    }

    private boolean alreadyFlagged(String transactionId, AnomalyType type) {
        return anomalyRepository.existsByTransactionIdAndType(transactionId, type);
    }

    private Anomaly buildAnomaly(Transaction tx, AnomalyType type, String reason) {
        Anomaly anomaly = new Anomaly();
        anomaly.setTransactionId(tx.getId());
        anomaly.setSource(tx.getSource());
        anomaly.setAmount(tx.getAmount());
        anomaly.setType(type);
        anomaly.setReason(reason);
        return anomaly;
    }
}
