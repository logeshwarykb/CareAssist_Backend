package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.response.DocumentRequestResponseDto;
import com.springboot.medical_billing.model.DocumentRequest;
import org.springframework.stereotype.Component;

@Component
public class DocumentRequestMapper {

    public static DocumentRequestResponseDto convertEntityToDto(DocumentRequest request) {
        return new DocumentRequestResponseDto(
                request.getId(),
                request.getPatient().getId(),
                request.getPatient().getFullName(),
                request.getHealthcareProvider().getId(),
                request.getHealthcareProvider().getName(),
                request.getRequestDate(),
                request.getStatus(),
                request.getInvoiceFile(),
                request.getMedicalFile(),
                request.getSentDate()
        );
    }
}
