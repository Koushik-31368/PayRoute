package com.payroute.transaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Records a single routing attempt for a Transaction.
 *
 * Why a separate entity?
 *   A transaction might be attempted on Provider A (timeout), then Provider B
 *   (failure), then finally succeed on Provider C. Each hop is a distinct row
 *   here. This gives you:
 *     - A full audit trail ("show me every provider call for txn-xyz")
 *     - Per-provider success/failure counts for the circuit breaker's rolling window
 *     - Average latency statistics per provider
 *
 * The circuit breaker queries this table (via TransactionAttemptRepository)
 * for the last N attempts per provider to compute its failure rate.
 */
@Entity
@Table(name = "transaction_attempts")
@Getter
@Setter
@NoArgsConstructor
public class TransactionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Parent transaction — many attempts belong to one transaction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /** Which provider was called (e.g. "PROVIDER_A"). */
    @Column(nullable = false)
    private String provider;

    /** Outcome of THIS attempt (not the overall transaction). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptResult result;

    /** How long the provider call took, in milliseconds. */
    @Column(nullable = false)
    private long latencyMs;

    /** Order of this attempt within its parent transaction (1-indexed). */
    @Column(nullable = false)
    private int attemptNumber;

    @Column(nullable = false)
    private Instant attemptedAt;

    /** Optional provider error message, truncated to 500 chars. */
    @Column(length = 500)
    private String errorMessage;

    @PrePersist
    void onCreate() {
        attemptedAt = Instant.now();
    }
}
