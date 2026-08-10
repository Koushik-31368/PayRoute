package com.payroute.websocket;

import com.payroute.transaction.Transaction;
import com.payroute.transaction.TransactionAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Publishes real-time transaction events over WebSocket (STOMP protocol).
 *
 * How STOMP over WebSocket works:
 *   - STOMP is a simple text-based messaging protocol layered on top of WebSocket.
 *   - Clients subscribe to a "topic" (e.g. /topic/transactions).
 *   - When we call convertAndSend(), Spring broadcasts the message to all
 *     subscribed clients automatically — no manual connection tracking needed.
 *   - SockJS provides a fallback (long-polling) for browsers that don't support
 *     native WebSocket.
 *
 * This class is called by TransactionService after each transaction completes.
 * The payload sent to the client is a compact summary — not the full JPA entity
 * (which would cause lazy-loading issues and expose internal fields).
 */
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /** Topic the frontend subscribes to for the live feed. */
    private static final String TRANSACTION_TOPIC = "/topic/transactions";

    /** Topic for anomaly alerts. */
    private static final String ANOMALY_TOPIC = "/topic/anomalies";

    /**
     * Broadcast a transaction completion event.
     * Builds a DTO-style map instead of serializing the JPA entity directly
     * to avoid Hibernate lazy-loading surprises with Jackson serialization.
     */
    public void publishTransactionComplete(Transaction transaction) {
        List<Map<String, Object>> attempts = transaction.getAttempts().stream()
                .map(a -> Map.<String, Object>of(
                        "provider", a.getProvider(),
                        "result", a.getResult().name(),
                        "latencyMs", a.getLatencyMs(),
                        "attemptNumber", a.getAttemptNumber()
                ))
                .toList();

        Map<String, Object> event = Map.of(
                "type", "TRANSACTION_COMPLETE",
                "id", transaction.getId(),
                "amount", transaction.getAmount(),
                "source", transaction.getSource(),
                "status", transaction.getStatus().name(),
                "finalProvider", transaction.getFinalProvider() != null ? transaction.getFinalProvider() : "",
                "createdAt", transaction.getCreatedAt().toString(),
                "attempts", attempts
        );

        messagingTemplate.convertAndSend(TRANSACTION_TOPIC, event);
    }

    /** Broadcast when a new anomaly is detected. */
    public void publishAnomaly(com.payroute.anomaly.Anomaly anomaly) {
        Map<String, Object> event = Map.of(
                "type", "ANOMALY_DETECTED",
                "id", anomaly.getId(),
                "transactionId", anomaly.getTransactionId(),
                "source", anomaly.getSource(),
                "amount", anomaly.getAmount(),
                "anomalyType", anomaly.getType().name(),
                "reason", anomaly.getReason(),
                "detectedAt", anomaly.getDetectedAt().toString()
        );

        messagingTemplate.convertAndSend(ANOMALY_TOPIC, event);
    }
}
