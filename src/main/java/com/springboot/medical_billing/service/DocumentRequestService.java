package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.DocumentRequestDto;
import com.springboot.medical_billing.dto.response.DocumentRequestResponseDto;
import com.springboot.medical_billing.enums.RequestStatus;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.DocumentRequestMapper;
import com.springboot.medical_billing.model.DocumentRequest;
import com.springboot.medical_billing.model.HealthcareProvider;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.repository.DocumentRequestRepository;
import com.springboot.medical_billing.repository.HealthcareProviderRepository;
import com.springboot.medical_billing.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRequestService {

    private final DocumentRequestRepository documentRequestRepository;
    private final PatientRepository patientRepository;
    private final HealthcareProviderRepository providerRepository;
    private final NotificationService notificationService;

    Logger logger = LoggerFactory.getLogger(DocumentRequestService.class);

    // patient asks the hospital to send invoice + medical document
    public DocumentRequestResponseDto addRequest(DocumentRequestDto dto, String patientUsername) {
        // Step 1: Fetch logged-in patient and the hospital
        Patient patient = patientRepository.getByUsername(patientUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        HealthcareProvider provider = providerRepository.findById(dto.providerId())
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare provider not found"));

        // Step 2: Save request as PENDING
        DocumentRequest request = new DocumentRequest();
        request.setPatient(patient);
        request.setHealthcareProvider(provider);
        request.setRequestDate(LocalDate.now());
        request.setStatus(RequestStatus.PENDING);
        DocumentRequest savedRequest = documentRequestRepository.save(request);

        // Step 3: Notify the hospital
        notificationService.add(provider.getUser(),
                patient.getFullName() + " (Patient ID " + patient.getId() + ") requested invoice and medical documents");
        logger.info("Document request raised by {} to {}", patientUsername, provider.getName());

        return DocumentRequestMapper.convertEntityToDto(savedRequest);
    }

    public List<DocumentRequestResponseDto> getPatientRequests(String patientUsername) {
        return documentRequestRepository.getByPatientUsername(patientUsername)
                .stream()
                .map(DocumentRequestMapper::convertEntityToDto)
                .toList();
    }

    public List<DocumentRequestResponseDto> getProviderRequests(String providerUsername) {
        return documentRequestRepository.getByProviderUsername(providerUsername)
                .stream()
                .map(DocumentRequestMapper::convertEntityToDto)
                .toList();
    }

    // hospital sent the documents -> request is COMPLETED
    public DocumentRequestResponseDto completeRequest(Integer id, String providerUsername) {
        // Step 1: Fetch request
        DocumentRequest request = documentRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document request not found"));

        // Step 2: Only the hospital this request was sent to
        if (!(request.getHealthcareProvider().getUser().getUsername().equals(providerUsername))) {
            throw new OwnershipException("Operation not allowed! This request is not sent to you");
        }

        // Step 3: Keep a copy of the documents just sent, with date and time
        request.setInvoiceFile(request.getPatient().getInvoiceFile());
        request.setMedicalFile(request.getPatient().getMedicalFile());
        request.setSentDate(LocalDateTime.now());

        // Step 4: Mark completed and save
        request.setStatus(RequestStatus.COMPLETED);
        logger.info("Document request {} completed by {}", id, providerUsername);
        return DocumentRequestMapper.convertEntityToDto(documentRequestRepository.save(request));
    }
}
