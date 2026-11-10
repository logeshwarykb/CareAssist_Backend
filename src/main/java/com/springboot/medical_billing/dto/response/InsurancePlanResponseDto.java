package com.springboot.medical_billing.dto.response;

public record InsurancePlanResponseDto(
        Integer id,
        String name,
        double coverageAmount,
        double premiumAmount,
        String coverageDetails,
        String termsConditions,
        Integer executiveId,   // insurance company that created the plan
        String executiveName
) {
}
