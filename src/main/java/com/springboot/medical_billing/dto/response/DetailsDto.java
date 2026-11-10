package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.Role;

public record DetailsDto(
        String username,
        Boolean isActive,
        Role role
) {
}