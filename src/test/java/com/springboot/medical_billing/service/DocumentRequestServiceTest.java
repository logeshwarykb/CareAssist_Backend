package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.DocumentRequestDto;
import com.springboot.medical_billing.dto.response.DocumentRequestResponseDto;
import com.springboot.medical_billing.enums.RequestStatus;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.DocumentRequest;
import com.springboot.medical_billing.model.HealthcareProvider;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.DocumentRequestRepository;
import com.springboot.medical_billing.repository.HealthcareProviderRepository;
import com.springboot.medical_billing.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class DocumentRequestServiceTest {

    @InjectMocks
    private DocumentRequestService documentRequestService; // The REAL service being tested
    @Mock
    private DocumentRequestRepository documentRequestRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private HealthcareProviderRepository providerRepository;
    @Mock
    private NotificationService notificationService;

    // helper: patient "john"
    private Patient getPatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setFullName("John Doe");
        patient.setUser(new User("john", "encodedPassword", Role.PATIENT));
        return patient;
    }

    // helper: hospital "apollo"
    private HealthcareProvider getProvider() {
        HealthcareProvider provider = new HealthcareProvider();
        provider.setId(1);
        provider.setName("Apollo Hospital");
        provider.setUser(new User("apollo", "encodedPassword", Role.HEALTHCARE_PROVIDER));
        return provider;
    }

    // helper: request from john to apollo
    private DocumentRequest getRequest(RequestStatus status) {
        DocumentRequest request = new DocumentRequest();
        request.setId(1);
        request.setPatient(getPatient());
        request.setHealthcareProvider(getProvider());
        request.setRequestDate(LocalDate.now());
        request.setStatus(status);
        return request;
    }

    // patient "john" asks apollo for documents, so request is saved as PENDING and hospital is notified
    @Test
    public void addRequestTest() {
        Patient patient = getPatient();
        HealthcareProvider provider = getProvider();
        DocumentRequestDto dto = new DocumentRequestDto(1);

        when(patientRepository.getByUsername("john")).thenReturn(Optional.of(patient));
        when(providerRepository.findById(1)).thenReturn(Optional.of(provider));
        // return the same request object the service saves
        when(documentRequestRepository.save(any(DocumentRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentRequestResponseDto result = documentRequestService.addRequest(dto, "john");

        assertEquals(RequestStatus.PENDING, result.status());
        assertEquals("Apollo Hospital", result.providerName());
        assertEquals(LocalDate.now(), result.requestDate());

        verify(patientRepository, times(1)).getByUsername("john");
        verify(providerRepository, times(1)).findById(1);
        verify(documentRequestRepository, times(1)).save(any(DocumentRequest.class));
        verify(notificationService, times(1)).add(eq(provider.getUser()), anyString());
        verifyNoMoreInteractions(documentRequestRepository);
    }

    // hospital id 7 doesn't exist, so request is not saved
    @Test
    public void addRequestTest_InvalidProvider() {
        DocumentRequestDto dto = new DocumentRequestDto(7);

        when(patientRepository.getByUsername("john")).thenReturn(Optional.of(getPatient()));
        when(providerRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> documentRequestService.addRequest(dto, "john"));

        verify(documentRequestRepository, times(0)).save(any(DocumentRequest.class));
        verifyNoMoreInteractions(documentRequestRepository);
        verifyNoMoreInteractions(notificationService);
    }

    // hospital "apollo" has 2 requests
    @Test
    public void getProviderRequestsTest() {
        when(documentRequestRepository.getByProviderUsername("apollo"))
                .thenReturn(List.of(getRequest(RequestStatus.PENDING), getRequest(RequestStatus.COMPLETED)));

        assertEquals(2, documentRequestService.getProviderRequests("apollo").size());

        verify(documentRequestRepository, times(1)).getByProviderUsername("apollo");
        verifyNoMoreInteractions(documentRequestRepository);
    }

    // apollo sent the documents, so the request becomes COMPLETED
    @Test
    public void completeRequestTest() {
        DocumentRequest request = getRequest(RequestStatus.PENDING);
        // files already uploaded to the patient by the hospital
        request.getPatient().setInvoiceFile("bill.pdf");
        request.getPatient().setMedicalFile("report.pdf");

        when(documentRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(documentRequestRepository.save(request)).thenReturn(request);

        DocumentRequestResponseDto result = documentRequestService.completeRequest(1, "apollo");

        assertEquals(RequestStatus.COMPLETED, result.status());
        assertEquals("bill.pdf", result.invoiceFile());
        assertEquals("report.pdf", result.medicalFile());
        assertNotNull(result.sentDate());

        verify(documentRequestRepository, times(1)).findById(1);
        verify(documentRequestRepository, times(1)).save(request);
        verifyNoMoreInteractions(documentRequestRepository);
    }

    // another hospital "fortis" tries to complete apollo's request, so it is blocked
    @Test
    public void completeRequestTest_Ownership() {
        DocumentRequest request = getRequest(RequestStatus.PENDING);

        when(documentRequestRepository.findById(1)).thenReturn(Optional.of(request));

        assertThrows(OwnershipException.class,
                () -> documentRequestService.completeRequest(1, "fortis"));

        verify(documentRequestRepository, times(1)).findById(1);
        verify(documentRequestRepository, times(0)).save(any(DocumentRequest.class));
        verifyNoMoreInteractions(documentRequestRepository);
    }
}
