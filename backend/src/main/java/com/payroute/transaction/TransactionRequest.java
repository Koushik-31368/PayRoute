package com.payroute.transaction;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Inbound DTO for POST /api/transactions.
 *
 * idempotencyKey: caller-generated UUID. The same key on a retry returns the
 * original result without re-processing. If the caller doesn't supply one,
 * they're opting out of idempotency protection — the controller can also
 * auto-generate one, but making it explicit teaches callers the pattern.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "source is required")
    @Size(max = 100, message = "source must be at most 100 characters")
    private String source;

    /**
     * Optional — if omitted, the TransactionService generates a UUID.
     * Best practice: let clients supply this so retries after network
     * timeouts are automatically safe.
     */
    @Size(max = 100, message = "idempotencyKey must be at most 100 characters")
    private String idempotencyKey;
}
