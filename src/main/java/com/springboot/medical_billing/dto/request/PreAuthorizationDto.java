package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PreAuthorizationDto(

        @NotNull(message = "Patient ID is required")
        Integer patientId,

        @NotNull(message = "Policy ID is required")
        Integer policyId,

        @NotNull(message = "Provider ID is required")
        Integer providerId,

        @NotNull(message = "TPA ID is required")
        Integer tpaId,

        @NotBlank(message = "Diagnosis is required")
        String diagnosis,

        @NotNull(message = "Estimated amount is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Estimated amount must be greater than zero")
        Double estimatedAmount
) {
}
