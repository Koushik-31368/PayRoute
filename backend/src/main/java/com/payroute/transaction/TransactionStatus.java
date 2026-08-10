package com.payroute.transaction;

/**
 * Final status of a Transaction (the user-facing result).
 *
 * SUCCESS  — at least one provider accepted the payment.
 * FAILED   — all providers were tried (or unavailable) and none succeeded.
 * PENDING  — the transaction row has been created but routing is still in
 *             progress. Useful if you later make the routing async.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
