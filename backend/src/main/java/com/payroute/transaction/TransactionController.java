package com.payroute.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the transaction lifecycle.
 *
 * POST /api/transactions  — Submit a payment request
 * GET  /api/transactions  — List recent transactions (for initial dashboard load)
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    /**
     * Submit a payment.
     *
     * Returns 201 CREATED for new transactions, 200 OK for idempotent replays.
     * The client can tell the difference from the HTTP status code, or by
     * comparing the transaction's createdAt vs now.
     */
    @PostMapping
    public ResponseEntity<Transaction> submitTransaction(
            @Valid @RequestBody TransactionRequest request) {

        boolean isNewKey = request.getIdempotencyKey() == null ||
                           transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()).isEmpty();

        Transaction result = transactionService.process(request);

        HttpStatus status = isNewKey ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Returns the 50 most recent transactions for the initial dashboard load.
     * Ongoing updates come via WebSocket.
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        return ResponseEntity.ok(transactionRepository.findTop50ByOrderByCreatedAtDesc());
    }
}
