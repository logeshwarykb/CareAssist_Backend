package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InsurancePlanDto(

        @NotBlank(message = "Plan name is required")
        String name,

        @NotNull(message = "Coverage amount is required")
        @Min(value = 1,
                message = "Coverage amount must be greater than zero")
        Double coverageAmount,

        @NotNull(message = "Premium amount is required")
        @Min(value = 1,
                message = "Premium amount must be greater than zero")
        Double premiumAmount,

        @NotBlank(message = "Coverage details are required")
        String coverageDetails,

        @NotBlank(message = "Terms and conditions are required")
        String termsConditions
) {
}