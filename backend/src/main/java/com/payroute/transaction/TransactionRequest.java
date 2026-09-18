package com.payroute.transaction;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Inbound DTO for POST /api/transactions.
 *
 * <p><b>idempotencyKey</b>: caller-generated UUID. The same key on a retry returns the
 * original result without re-processing. If the caller does not supply one,
 * they are opting out of idempotency protection – the controller can also
 * auto-generate one, but making it explicit teaches callers the pattern.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    /** Payment amount in INR. Must be positive and at most 10 crore (₹10,000,000). */
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @DecimalMax(value = "10000000.00", message = "amount must not exceed 10,000,000")
    @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "source is required")
    @Size(max = 100, message = "source must be at most 100 characters")
    private String source;

    /**
     * Optional – if omitted, the TransactionService generates a UUID.
     * Best practice: let clients supply this so retries after network
     * timeouts are automatically safe.
     */
    @Size(max = 100, message = "idempotencyKey must be at most 100 characters")
    private String idempotencyKey;
}
