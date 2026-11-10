package com.springboot.medical_billing.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PatientPolicyDto(

        @NotNull(message = "Patient ID is required")
        Integer patientId,

        @NotNull(message = "Insurance plan ID is required")
        Integer planId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
}