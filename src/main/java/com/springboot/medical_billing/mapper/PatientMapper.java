package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.PatientDto;
import com.springboot.medical_billing.dto.response.PatientResponseDto;
import com.springboot.medical_billing.model.Patient;

public class PatientMapper {

    public static Patient convertDtoToEntity(PatientDto patientDto) {
        Patient patient = new Patient();
        patient.setFullName(patientDto.fullName());
        patient.setDateOfBirth(patientDto.dateOfBirth());
        patient.setGender(patientDto.gender());
        patient.setContactNumber(patientDto.contactNumber());
        patient.setMedicalHistory(patientDto.medicalHistory());
        return patient;
    }


    public static PatientResponseDto convertEntityToDto(Patient patient) {
        return new PatientResponseDto(
                patient.getId(),
                patient.getUser().getId(),
                patient.getUser().getUsername(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getContactNumber(),
                patient.getMedicalHistory(),
                patient.getInvoiceFile(),
                patient.getMedicalFile()
        );
    }
}