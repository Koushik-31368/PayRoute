package com.payroute.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionAttemptRepository extends JpaRepository<TransactionAttempt, String> {

    /**
     * Circuit breaker query: fetch the last N attempts for a given provider,
     * ordered newest-first. The circuit breaker uses this rolling window to
     * compute the failure rate.
     *
     * JPQL LIMIT is not standard; using a named query with a native slice
     * instead via Spring Data's built-in Pageable, but for clarity here we
     * use a simple findTop approach.
     */
    List<TransactionAttempt> findTopNByProviderOrderByAttemptedAtDesc(
            String provider, org.springframework.data.domain.Pageable pageable);

    /**
     * Stats endpoint: average latency per provider across all SUCCESS attempts.
     * Returns a List<Object[]> where [0]=provider, [1]=avgLatency.
     */
    @Query("""
        SELECT a.provider, AVG(a.latencyMs)
        FROM TransactionAttempt a
        WHERE a.result = 'SUCCESS'
        GROUP BY a.provider
        """)
    List<Object[]> findAvgLatencyByProvider();

    /**
     * Stats endpoint: success rate per provider.
     * Returns List<Object[]> where [0]=provider, [1]=total, [2]=successes.
     */
    @Query("""
        SELECT a.provider,
               COUNT(a),
               SUM(CASE WHEN a.result = 'SUCCESS' THEN 1 ELSE 0 END)
        FROM TransactionAttempt a
        WHERE a.result != 'SKIPPED'
        GROUP BY a.provider
        """)
    List<Object[]> findSuccessRateByProvider();
}
