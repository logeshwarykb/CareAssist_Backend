package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.PreAuthStatus;

import java.time.LocalDate;

public record PreAuthorizationResponseDto(
        Integer id,
        Integer patientId,
        String patientName,
        Integer policyId,
        Integer providerId,
        String providerName,
        Integer tpaId,
        String tpaName,
        String diagnosis,
        Double estimatedAmount,
        LocalDate requestDate,
        PreAuthStatus status,
        Integer claimId
) {
}
