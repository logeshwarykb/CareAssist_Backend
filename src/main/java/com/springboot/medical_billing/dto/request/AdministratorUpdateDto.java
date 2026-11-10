package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdministratorUpdateDto(
        @NotBlank(message = "Name is required")
        String name
) {
}