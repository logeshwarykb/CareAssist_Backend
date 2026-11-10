package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TpaDto(
        @NotBlank
        String username,

        @NotBlank
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password,

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = "\\d{8,12}", message = "Contact number must be 8 to 12 digits")
        String contactNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
