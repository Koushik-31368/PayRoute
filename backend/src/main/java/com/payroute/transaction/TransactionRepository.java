package com.payroute.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /** Idempotency check — find an existing transaction by its idempotency key. */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /** Anomaly detection: count how many transactions a source has submitted
     *  within a recent time window (burst detection). */
    long countBySourceAndCreatedAtAfter(String source, Instant since);

    /** Anomaly detection: find recent failed transactions from the same source. */
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.source = :source
          AND t.status = 'FAILED'
          AND t.createdAt >= :since
        """)
    List<Transaction> findRecentFailuresBySource(@Param("source") String source,
                                                  @Param("since") Instant since);

    /** Dashboard: fetch the latest N transactions for the initial page load. */
    List<Transaction> findTop50ByOrderByCreatedAtDesc();
}
