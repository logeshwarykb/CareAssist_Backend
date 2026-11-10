package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ClaimDto(

        @NotNull(message = "Patient ID is required")
        Integer patientId,

        @NotNull(message = "Policy ID is required")
        Integer policyId,

        @NotNull(message = "Insurance executive ID is required")
        Integer executiveId,

        @NotNull(message = "Claim date is required")
        LocalDate claimDate,

        @NotNull(message = "Claim amount is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Claim amount must be greater than zero")
        Double claimAmount
) {
}
