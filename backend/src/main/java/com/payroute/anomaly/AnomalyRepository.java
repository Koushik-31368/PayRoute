package com.payroute.anomaly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, String> {

    /** Dashboard: return the 100 most recent anomalies. */
    List<Anomaly> findTop100ByOrderByDetectedAtDesc();

    /** Check if a specific transaction has already been flagged for a given type. */
    boolean existsByTransactionIdAndType(String transactionId, AnomalyType type);
}
