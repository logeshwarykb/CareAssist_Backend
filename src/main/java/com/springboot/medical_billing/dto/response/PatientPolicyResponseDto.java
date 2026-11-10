package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.PolicyStatus;

import java.time.LocalDate;

public record PatientPolicyResponseDto(
        Integer id,
        Integer patientId,
        String patientName,
        Integer planId,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        PolicyStatus status,
        Integer executiveId,
        String companyName
) {
}