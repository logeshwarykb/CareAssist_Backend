package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentRequestResponseDto(
        Integer id,
        Integer patientId,
        String patientName,
        Integer providerId,
        String providerName,
        LocalDate requestDate,
        RequestStatus status,
        String invoiceFile,
        String medicalFile,
        LocalDateTime sentDate
) {
}
