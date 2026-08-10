package com.payroute.anomaly;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An Anomaly record is created whenever a transaction triggers one of the
 * rule-based detection checks in {@link AnomalyDetectionService}.
 *
 * Why a separate table (not a flag on Transaction)?
 *   A single transaction could trigger multiple anomaly rules simultaneously
 *   (e.g., both "large amount" AND "burst from same source"). A separate table
 *   with one row per rule violation gives you richer audit data and makes the
 *   GET /api/anomalies endpoint trivial to implement.
 */
@Entity
@Table(name = "anomalies")
@Getter
@Setter
@NoArgsConstructor
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** The transaction that triggered this anomaly. */
    @Column(nullable = false)
    private String transactionId;

    /** Source identifier from the transaction (for quick filtering). */
    @Column(nullable = false)
    private String source;

    /** Amount from the transaction. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Which rule fired. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnomalyType type;

    /** Human-readable description of why this was flagged. */
    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private Instant detectedAt;

    @PrePersist
    void onCreate() {
        detectedAt = Instant.now();
    }
}
