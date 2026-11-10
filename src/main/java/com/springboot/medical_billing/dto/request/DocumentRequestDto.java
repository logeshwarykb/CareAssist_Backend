package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.NotNull;

public record DocumentRequestDto(

        @NotNull(message = "Hospital is required")
        Integer providerId
) {
}
