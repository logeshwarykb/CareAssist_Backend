package com.springboot.medical_billing.dto.request;

import com.springboot.medical_billing.enums.ProviderType;
import com.springboot.medical_billing.enums.Specialization;
import jakarta.validation.constraints.NotNull;

public record HealthcareProviderUpdateDto(
        @NotNull(message = "Name is required")
        String name,
        @NotNull(message = "ProviderType is required")
        ProviderType providerType,
        @NotNull(message = "Specialization is required")
        Specialization specialization,
        @NotNull(message = "Contact number is required")
        String contactNumber,
        @NotNull(message = "Address is required")
        String address
) {
}
