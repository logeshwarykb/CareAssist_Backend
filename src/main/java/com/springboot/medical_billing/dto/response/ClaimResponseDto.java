package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;

import java.time.LocalDate;

public record ClaimResponseDto(
        Integer id,
        Integer patientId,
        String patientName,
        Integer policyId,
        Integer executiveId,
        String executiveName,
        LocalDate claimDate,
        Double claimAmount,
        ClaimStatus status,
        ClaimType claimType,
        Integer tpaId,
        String tpaName,
        Integer preAuthorizationId,
        Double approvedAmount,
        String providerName,
        Integer policyCompanyId,
        String policyCompanyName,
        String invoiceFile,
        String medicalFile,
        LocalDate approvedDate
) {
}
