package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.ProviderType;
import com.springboot.medical_billing.enums.Specialization;

public record HealthcareProviderResponseDto(
        Integer id,
        Integer userId,
        String username,
        String name,
        ProviderType providerType,
        Specialization specialization,
        String contactNumber,
        String address
) {
}