package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PreAuthorizationDto;
import com.springboot.medical_billing.dto.response.PreAuthorizationResponseDto;
import com.springboot.medical_billing.enums.PolicyStatus;
import com.springboot.medical_billing.enums.PreAuthStatus;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.PreAuthorizationMapper;
import com.springboot.medical_billing.model.*;
import com.springboot.medical_billing.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class PreAuthorizationServiceTest {

    @InjectMocks
    private PreAuthorizationService preAuthorizationService; // The REAL service being tested
    @Mock
    private PreAuthorizationRepository preAuthorizationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientPolicyRepository patientPolicyRepository;
    @Mock
    private HealthcareProviderRepository providerRepository;
    @Mock
    private TpaRepository tpaRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private PreAuthorizationMapper preAuthorizationMapper;
    @Mock
    private NotificationService notificationService;

    private Patient getPatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setFullName("John Doe");
        patient.setUser(new User("john", "encodedPassword", Role.PATIENT));
        return patient;
    }

    private HealthcareProvider getProvider() {
        HealthcareProvider provider = new HealthcareProvider();
        provider.setId(1);
        provider.setName("Apollo Hospital");
        provider.setUser(new User("apollo", "encodedPassword", Role.HEALTHCARE_PROVIDER));
        return provider;
    }

    private Tpa getTpa() {
        Tpa tpa = new Tpa();
        tpa.setId(1);
        tpa.setName("Medi Assist");
        tpa.setUser(new User("mediassist", "encodedPassword", Role.TPA));
        return tpa;
    }

    // active policy that started yesterday and ends after one year
    private PatientPolicy getPolicy(Patient patient) {
        PatientPolicy policy = new PatientPolicy();
        policy.setId(1);
        policy.setPatient(patient);
        policy.setStartDate(LocalDate.now().minusDays(1));
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setStatus(PolicyStatus.ACTIVE);
        return policy;
    }

    private PreAuthorizationResponseDto getResponseDto(PreAuthStatus status, Integer claimId) {
        return new PreAuthorizationResponseDto(1, 1, "John Doe", 1, 1, "Apollo Hospital",
                1, "Medi Assist", "Appendicitis", 50000.0, LocalDate.now(), status, claimId);
    }

    // hospital "apollo" sends pre-auth for john's active policy, so it is saved as PENDING
    @Test
    public void insertPreAuthorizationTest() {
        Patient patient = getPatient();
        PreAuthorizationDto dto = new PreAuthorizationDto(1, 1, 1, 1, "Appendicitis", 50000.0);
        PreAuthorization preAuthorization = new PreAuthorization();

        when(providerRepository.findById(1)).thenReturn(Optional.of(getProvider()));
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientPolicyRepository.findById(1)).thenReturn(Optional.of(getPolicy(patient)));
        when(tpaRepository.findById(1)).thenReturn(Optional.of(getTpa()));
        when(preAuthorizationMapper.convertDtoToEntity(dto)).thenReturn(preAuthorization);
        when(preAuthorizationRepository.save(preAuthorization)).thenReturn(preAuthorization);
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, null))
                .thenReturn(getResponseDto(PreAuthStatus.PENDING, null));

        PreAuthorizationResponseDto result =
                preAuthorizationService.insertPreAuthorization(dto, "apollo");

        assertEquals(PreAuthStatus.PENDING, preAuthorization.getStatus());
        assertEquals(patient, preAuthorization.getPatient());
        assertEquals(PreAuthStatus.PENDING, result.status());

        verify(providerRepository, times(1)).findById(1);
        verify(patientRepository, times(1)).findById(1);
        verify(patientPolicyRepository, times(1)).findById(1);
        verify(tpaRepository, times(1)).findById(1);
        verify(preAuthorizationRepository, times(1)).save(preAuthorization);
        // TPA gets a notification
        verify(notificationService, times(1)).add(any(), anyString());
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // another hospital "fortis" tries to send the request as apollo, so it is blocked
    @Test
    public void insertPreAuthorizationTest_Ownership() {
        PreAuthorizationDto dto = new PreAuthorizationDto(1, 1, 1, 1, "Appendicitis", 50000.0);

        when(providerRepository.findById(1)).thenReturn(Optional.of(getProvider()));

        assertThrows(OwnershipException.class,
                () -> preAuthorizationService.insertPreAuthorization(dto, "fortis"));

        verify(providerRepository, times(1)).findById(1);
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // policy is INACTIVE, so the pre-auth request is not saved
    @Test
    public void insertPreAuthorizationTest_InactivePolicy() {
        Patient patient = getPatient();
        PatientPolicy policy = getPolicy(patient);
        policy.setStatus(PolicyStatus.INACTIVE);
        PreAuthorizationDto dto = new PreAuthorizationDto(1, 1, 1, 1, "Appendicitis", 50000.0);

        when(providerRepository.findById(1)).thenReturn(Optional.of(getProvider()));
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientPolicyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(tpaRepository.findById(1)).thenReturn(Optional.of(getTpa()));

        assertThrows(IllegalArgumentException.class,
                () -> preAuthorizationService.insertPreAuthorization(dto, "apollo"));

        verify(preAuthorizationRepository, times(0)).save(any(PreAuthorization.class));
        verifyNoMoreInteractions(preAuthorizationRepository);
        verifyNoMoreInteractions(preAuthorizationMapper);
    }

    // TPA "mediassist" gives initial approval, so a CASHLESS claim is created and its ID is returned
    @Test
    public void updatePreAuthStatusTest() {
        Patient patient = getPatient();
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);
        preAuthorization.setPatient(patient);
        preAuthorization.setPolicy(getPolicy(patient));
        preAuthorization.setHealthcareProvider(getProvider());
        preAuthorization.setTpa(getTpa());
        preAuthorization.setEstimatedAmount(50000.0);
        preAuthorization.setStatus(PreAuthStatus.PENDING);

        Claim savedClaim = new Claim();
        savedClaim.setId(10);

        when(preAuthorizationRepository.findById(1)).thenReturn(Optional.of(preAuthorization));
        when(preAuthorizationRepository.save(preAuthorization)).thenReturn(preAuthorization);
        when(claimRepository.save(any(Claim.class))).thenReturn(savedClaim);
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, 10))
                .thenReturn(getResponseDto(PreAuthStatus.APPROVED, 10));

        PreAuthorizationResponseDto result =
                preAuthorizationService.updatePreAuthStatus(1, PreAuthStatus.APPROVED, "mediassist");

        assertEquals(PreAuthStatus.APPROVED, preAuthorization.getStatus());
        assertEquals(10, result.claimId());

        verify(preAuthorizationRepository, times(1)).findById(1);
        verify(preAuthorizationRepository, times(1)).save(preAuthorization);
        verify(claimRepository, times(1)).save(any(Claim.class));
        // hospital and patient get notifications
        verify(notificationService, times(2)).add(any(), anyString());
        verifyNoMoreInteractions(preAuthorizationRepository);
        verifyNoMoreInteractions(claimRepository);
    }

    // TPA rejects the pre-auth, so NO claim is created
    @Test
    public void updatePreAuthStatusTest_Rejected() {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);
        preAuthorization.setPatient(getPatient());
        preAuthorization.setHealthcareProvider(getProvider());
        preAuthorization.setTpa(getTpa());
        preAuthorization.setStatus(PreAuthStatus.PENDING);

        when(preAuthorizationRepository.findById(1)).thenReturn(Optional.of(preAuthorization));
        when(preAuthorizationRepository.save(preAuthorization)).thenReturn(preAuthorization);
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, null))
                .thenReturn(getResponseDto(PreAuthStatus.REJECTED, null));

        PreAuthorizationResponseDto result =
                preAuthorizationService.updatePreAuthStatus(1, PreAuthStatus.REJECTED, "mediassist");

        assertEquals(PreAuthStatus.REJECTED, result.status());

        verify(preAuthorizationRepository, times(1)).findById(1);
        verify(preAuthorizationRepository, times(1)).save(preAuthorization);
        verify(claimRepository, times(0)).save(any(Claim.class));
        // hospital and patient get notifications
        verify(notificationService, times(2)).add(any(), anyString());
        verifyNoMoreInteractions(claimRepository);
    }

    // pre-auth is already REJECTED, so it cannot be approved again
    @Test
    public void updatePreAuthStatusTest_AlreadyDecided() {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);
        preAuthorization.setTpa(getTpa());
        preAuthorization.setStatus(PreAuthStatus.REJECTED);

        when(preAuthorizationRepository.findById(1)).thenReturn(Optional.of(preAuthorization));

        assertThrows(IllegalArgumentException.class,
                () -> preAuthorizationService.updatePreAuthStatus(1, PreAuthStatus.APPROVED, "mediassist"));

        verify(preAuthorizationRepository, times(1)).findById(1);
        verify(preAuthorizationRepository, times(0)).save(any(PreAuthorization.class));
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // TPA 1 exists and has 1 pre-auth request (not approved yet, so no claim ID)
    @Test
    public void getPreAuthorizationsByTpaIdTest() {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);

        when(tpaRepository.existsById(1)).thenReturn(true);
        when(preAuthorizationRepository.getPreAuthorizationsByTpaId(1, PageRequest.of(0, 5)))
                .thenReturn(List.of(preAuthorization));
        when(claimRepository.getByPreAuthorizationId(1)).thenReturn(Optional.empty());
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, null))
                .thenReturn(getResponseDto(PreAuthStatus.PENDING, null));

        assertEquals(1, preAuthorizationService.getPreAuthorizationsByTpaId(1, 0, 5).size());

        verify(tpaRepository, times(1)).existsById(1);
        verify(preAuthorizationRepository, times(1)).getPreAuthorizationsByTpaId(1, PageRequest.of(0, 5));
        verifyNoMoreInteractions(tpaRepository);
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // TPA 7 doesn't exist (invalid id), so requests are never fetched
    @Test
    public void getPreAuthorizationsByTpaIdTest_Invalid() {
        when(tpaRepository.existsById(7)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> preAuthorizationService.getPreAuthorizationsByTpaId(7, 0, 5));

        verify(tpaRepository, times(1)).existsById(7);
        verify(preAuthorizationRepository, times(0)).getPreAuthorizationsByTpaId(7, PageRequest.of(0, 5));
        verifyNoMoreInteractions(tpaRepository);
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // v1: patient 1 exists and has 1 pre-auth request
    @Test
    public void getPreAuthorizationsByPatientIdTest() {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);

        when(patientRepository.existsById(1)).thenReturn(true);
        when(preAuthorizationRepository.getPreAuthorizationsByPatientId(1, PageRequest.of(0, 5)))
                .thenReturn(List.of(preAuthorization));
        when(claimRepository.getByPreAuthorizationId(1)).thenReturn(Optional.empty());
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, null))
                .thenReturn(getResponseDto(PreAuthStatus.PENDING, null));

        assertEquals(1, preAuthorizationService.getPreAuthorizationsByPatientId(1, 0, 5).size());

        verify(patientRepository, times(1)).existsById(1);
        verify(preAuthorizationRepository, times(1)).getPreAuthorizationsByPatientId(1, PageRequest.of(0, 5));
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // v1: patient 7 doesn't exist (invalid id), so requests are never fetched
    @Test
    public void getPreAuthorizationsByPatientIdTest_Invalid() {
        when(patientRepository.existsById(7)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> preAuthorizationService.getPreAuthorizationsByPatientId(7, 0, 5));

        verify(patientRepository, times(1)).existsById(7);
        verifyNoMoreInteractions(preAuthorizationRepository);
    }

    // v2: logged-in patient "john" has 1 approved pre-auth with claim 10
    @Test
    public void getMyPreAuthorizationsTest() {
        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);
        Claim claim = new Claim();
        claim.setId(10);

        when(preAuthorizationRepository.getPreAuthorizationsByPatientUsername("john", PageRequest.of(0, 5)))
                .thenReturn(List.of(preAuthorization));
        when(claimRepository.getByPreAuthorizationId(1)).thenReturn(Optional.of(claim));
        when(preAuthorizationMapper.convertEntityToDto(preAuthorization, 10))
                .thenReturn(getResponseDto(PreAuthStatus.APPROVED, 10));

        List<PreAuthorizationResponseDto> result = preAuthorizationService.getMyPreAuthorizations("john", 0, 5);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).claimId());

        verify(preAuthorizationRepository, times(1)).getPreAuthorizationsByPatientUsername("john", PageRequest.of(0, 5));
        verifyNoMoreInteractions(preAuthorizationRepository);
    }
}
