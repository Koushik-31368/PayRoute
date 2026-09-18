package com.payroute.transaction;

/**
 * Final status of a Transaction (the user-facing result).
 *
 * <ul>
 *   <li><b>PENDING</b>  – the transaction row has been created but routing is still in
 *                          progress. Useful if you later make the routing async.</li>
 *   <li><b>SUCCESS</b>  – at least one provider accepted the payment.</li>
 *   <li><b>FAILED</b>   – all providers were tried (or unavailable) and none succeeded.</li>
 * </ul>
 *
 * Ordering matches the natural lifecycle: PENDING -> SUCCESS | FAILED.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
