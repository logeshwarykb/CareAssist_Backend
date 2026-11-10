package com.springboot.medical_billing.dto.request;

import com.springboot.medical_billing.enums.ProviderType;
import com.springboot.medical_billing.enums.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HealthcareProviderDto(
        @NotBlank
        @NotNull
        String username,

        @NotBlank
        @NotNull
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password,

        @NotNull(message = "Name is required")
        String name,

        @NotNull(message = "ProviderType is required")
        ProviderType providerType,

        @NotNull(message = "Specialization is required")
        Specialization specialization,

        @NotNull(message = "Contact number is required")
        @Pattern(regexp = "\\d{8,12}", message = "Contact number must be 8 to 12 digits")
        String contactNumber,

        @NotNull(message = "Address is required")
        String address
) {
}