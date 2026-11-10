package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.ClaimStatus;

public record ClaimStatusCountDto(
        ClaimStatus status,
        Long count
) {
}