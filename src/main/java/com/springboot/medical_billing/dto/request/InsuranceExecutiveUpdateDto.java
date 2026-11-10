package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InsuranceExecutiveUpdateDto(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Contact number is required")
        String contactNumber,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
