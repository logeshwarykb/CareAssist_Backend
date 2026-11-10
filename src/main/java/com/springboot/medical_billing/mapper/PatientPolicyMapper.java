package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.response.PatientPolicyResponseDto;
import com.springboot.medical_billing.model.PatientPolicy;

public class PatientPolicyMapper {

    public static PatientPolicyResponseDto convertEntityToDto(PatientPolicy patientPolicy) {
        return new PatientPolicyResponseDto(
                patientPolicy.getId(),
                patientPolicy.getPatient().getId(),
                patientPolicy.getPatient().getFullName(),
                patientPolicy.getPlan().getId(),
                patientPolicy.getPlan().getName(),
                patientPolicy.getStartDate(),
                patientPolicy.getEndDate(),
                patientPolicy.getStatus(),
                patientPolicy.getPlan().getExecutive() == null ? null : patientPolicy.getPlan().getExecutive().getId(),
                patientPolicy.getPlan().getExecutive() == null ? null : patientPolicy.getPlan().getExecutive().getName()
        );
    }
}