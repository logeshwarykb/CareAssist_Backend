package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PatientPolicyDto;
import com.springboot.medical_billing.dto.response.PatientPolicyResponseDto;
import com.springboot.medical_billing.enums.PolicyStatus;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.InsurancePlan;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.PatientPolicy;
import com.springboot.medical_billing.repository.InsurancePlanRepository;
import com.springboot.medical_billing.repository.PatientPolicyRepository;
import com.springboot.medical_billing.repository.PatientRepository;
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
public class PatientPolicyServiceTest {

    @InjectMocks
    private PatientPolicyService patientPolicyService; // The REAL service being tested
    @Mock
    private PatientPolicyRepository patientPolicyRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    // helper to prepare a full patient policy (patient + plan attached)
    private PatientPolicy getPolicy(Integer id, Patient patient, InsurancePlan plan) {
        PatientPolicy patientPolicy = new PatientPolicy();
        patientPolicy.setId(id);
        patientPolicy.setPatient(patient);
        patientPolicy.setPlan(plan);
        patientPolicy.setStartDate(LocalDate.of(2026, 1, 1));
        patientPolicy.setEndDate(LocalDate.of(2026, 12, 31));
        patientPolicy.setStatus(PolicyStatus.ACTIVE);
        return patientPolicy;
    }

    private Patient getPatient() {
        Patient patient = new Patient();
        patient.setId(1);
        patient.setFullName("John Doe");
        return patient;
    }

    private InsurancePlan getPlan() {
        InsurancePlan plan = new InsurancePlan();
        plan.setId(1);
        plan.setName("Gold Health");
        return plan;
    }

    // helper to prepare a policy response dto
    private PatientPolicyResponseDto getPolicyDto(Integer id) {
        return new PatientPolicyResponseDto(id, 1, "John Doe", 1, "Gold Health",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), PolicyStatus.ACTIVE, 1, "Star Health");
    }

    // patient 1 selects plan 1, so the policy is saved with status ACTIVE
    @Test
    public void insertPatientPolicyTest() {
        Patient patient = getPatient();
        InsurancePlan plan = getPlan();
        // policy starts today and ends after 365 days
        PatientPolicyDto policyDto = new PatientPolicyDto(1, 1,
                LocalDate.now(), LocalDate.now().plusDays(365));

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(patientPolicyRepository.save(any(PatientPolicy.class)))
                .thenReturn(getPolicy(1, patient, plan));

        PatientPolicyResponseDto result = patientPolicyService.insertPatientPolicy(policyDto);

        assertEquals(PolicyStatus.ACTIVE, result.status());
        assertEquals("John Doe", result.patientName());

        verify(patientRepository, times(1)).findById(1);
        verify(insurancePlanRepository, times(1)).findById(1);
        verify(patientPolicyRepository, times(1)).save(any(PatientPolicy.class));
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(insurancePlanRepository);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // end date is before start date (invalid dates), so the policy is not saved
    @Test
    public void insertPatientPolicyTest_InvalidDates() {
        // end date is before start date
        PatientPolicyDto policyDto = new PatientPolicyDto(1, 1,
                LocalDate.now().plusDays(10), LocalDate.now());

        assertThrows(IllegalArgumentException.class,
                () -> patientPolicyService.insertPatientPolicy(policyDto));

          verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(insurancePlanRepository);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // plan 7 doesn't exist (invalid id), so the policy is not saved
    @Test
    public void insertPatientPolicyTest_InvalidPlan() {
        PatientPolicyDto policyDto = new PatientPolicyDto(1, 7,
                LocalDate.now(), LocalDate.now().plusDays(365));

        when(patientRepository.findById(1)).thenReturn(Optional.of(getPatient()));
        when(insurancePlanRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientPolicyService.insertPatientPolicy(policyDto));

        verify(insurancePlanRepository, times(1)).findById(7);
        verify(patientPolicyRepository, times(0)).save(any(PatientPolicy.class));
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // fetching all policies: 2 policies in DB, so 2 results come back
    @Test
    public void getAllPatientPoliciesTest() {
        when(patientPolicyRepository.getAllPatientPolicies(PageRequest.of(0, 5)))
                .thenReturn(List.of(getPolicyDto(1), getPolicyDto(2)));

        assertEquals(2, patientPolicyService.getAllPatientPolicies(0, 5).size());

        verify(patientPolicyRepository, times(1)).getAllPatientPolicies(PageRequest.of(0, 5));
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // policy id 1 exists and the result has the correct plan and status
    @Test
    public void getByIdTest() {
        when(patientPolicyRepository.findById(1))
                .thenReturn(Optional.of(getPolicy(1, getPatient(), getPlan())));

        PatientPolicyResponseDto result = patientPolicyService.getById(1);

        assertEquals(1, result.planId());
        assertEquals(PolicyStatus.ACTIVE, result.status());

        verify(patientPolicyRepository, times(1)).findById(1);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // policy id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(patientPolicyRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientPolicyService.getById(7));

        verify(patientPolicyRepository, times(1)).findById(7);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // patient 1 exists, so his policies are returned
    @Test
    public void getPoliciesByPatientIdTest() {
        when(patientRepository.existsById(1)).thenReturn(true);
        when(patientPolicyRepository.getPoliciesByPatientId(1, PageRequest.of(0, 5)))
                .thenReturn(List.of(getPolicyDto(1)));

        assertEquals(1, patientPolicyService.getPoliciesByPatientId(1, 0, 5).size());

        verify(patientRepository, times(1)).existsById(1);
        verify(patientPolicyRepository, times(1)).getPoliciesByPatientId(1, PageRequest.of(0, 5));
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // patient 7 doesn't exist (invalid id), so policies are never fetched
    @Test
    public void getPoliciesByPatientIdTest_Invalid() {
        when(patientRepository.existsById(7)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> patientPolicyService.getPoliciesByPatientId(7, 0, 5));

        verify(patientRepository, times(1)).existsById(7);
        verify(patientPolicyRepository, times(0)).getPoliciesByPatientId(7, PageRequest.of(0, 5));
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(patientPolicyRepository);
    }

    // end date is not exactly 365 days after start date, so the policy is not saved
    @Test
    public void insertPatientPolicyTest_Not365Days() {
        PatientPolicyDto policyDto = new PatientPolicyDto(1, 1,
                LocalDate.now(), LocalDate.now().plusDays(100));

        assertThrows(IllegalArgumentException.class,
                () -> patientPolicyService.insertPatientPolicy(policyDto));

        verify(patientPolicyRepository, times(0)).save(any(PatientPolicy.class));
        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(insurancePlanRepository);
    }
}
