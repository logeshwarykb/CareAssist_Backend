package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CashlessClaimDto(

        @NotNull(message = "Claim amount is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Claim amount must be greater than zero")
        Double claimAmount
) {
}
