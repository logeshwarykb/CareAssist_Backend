package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.PreAuthorizationDto;
import com.springboot.medical_billing.dto.response.PreAuthorizationResponseDto;
import com.springboot.medical_billing.model.PreAuthorization;
import org.springframework.stereotype.Component;

@Component
public class PreAuthorizationMapper {

    public PreAuthorization convertDtoToEntity(PreAuthorizationDto dto) {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setDiagnosis(dto.diagnosis());
        preAuthorization.setEstimatedAmount(dto.estimatedAmount());
        return preAuthorization;
    }

    public PreAuthorizationResponseDto convertEntityToDto(PreAuthorization preAuthorization, Integer claimId) {
        return new PreAuthorizationResponseDto(
                preAuthorization.getId(),
                preAuthorization.getPatient().getId(),
                preAuthorization.getPatient().getFullName(),
                preAuthorization.getPolicy().getId(),
                preAuthorization.getHealthcareProvider().getId(),
                preAuthorization.getHealthcareProvider().getName(),
                preAuthorization.getTpa().getId(),
                preAuthorization.getTpa().getName(),
                preAuthorization.getDiagnosis(),
                preAuthorization.getEstimatedAmount(),
                preAuthorization.getRequestDate(),
                preAuthorization.getStatus(),
                claimId
        );
    }
}
