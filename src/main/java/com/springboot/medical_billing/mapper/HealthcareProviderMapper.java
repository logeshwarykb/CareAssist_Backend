package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.HealthcareProviderDto;
import com.springboot.medical_billing.dto.response.HealthcareProviderResponseDto;
import com.springboot.medical_billing.model.HealthcareProvider;

public class HealthcareProviderMapper {

    public static HealthcareProvider convertDtoToEntity(
            HealthcareProviderDto providerDto) {

        HealthcareProvider provider = new HealthcareProvider();
        provider.setName(providerDto.name());
        provider.setProviderType(providerDto.providerType());
        provider.setSpecialization(providerDto.specialization());
        provider.setContactNumber(providerDto.contactNumber());
        provider.setAddress(providerDto.address());

        return provider;
    }

    public static HealthcareProviderResponseDto convertEntityToDto(
            HealthcareProvider provider) {

        return new HealthcareProviderResponseDto(
                provider.getId(),
                provider.getUser().getId(),
                provider.getUser().getUsername(),
                provider.getName(),
                provider.getProviderType(),
                provider.getSpecialization(),
                provider.getContactNumber(),
                provider.getAddress()
        );
    }
}