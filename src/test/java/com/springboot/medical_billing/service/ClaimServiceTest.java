package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.CashlessClaimDto;
import com.springboot.medical_billing.dto.request.ClaimDto;
import com.springboot.medical_billing.dto.response.ChartDto;
import com.springboot.medical_billing.dto.response.ClaimFilterDto;
import com.springboot.medical_billing.dto.response.ClaimResponseDto;
import com.springboot.medical_billing.dto.response.ClaimStatusCountDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.model.*;
import com.springboot.medical_billing.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class ClaimServiceTest {

    @InjectMocks
    private ClaimService claimService; // The REAL service being tested
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientPolicyRepository patientPolicyRepository;
    @Mock
    private InsuranceExecutiveRepository insuranceExecutiveRepository;
    @Mock
    private TpaRepository tpaRepository;
    @Mock
    private HealthcareProviderRepository providerRepository;
    @Mock
    private NotificationService notificationService;

    // helper to prepare a patient who is linked to a login user
    private Patient getPatient(Integer id, String username) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFullName("John Doe");
        patient.setUser(new User(username, "encodedPassword", Role.PATIENT));
        return patient;
    }

    // helper: 1 year policy that started 10 days ago (today is inside the policy period)
    // plan belongs to insurance company 1 (Star Health)
    private PatientPolicy getPolicy(Patient patient) {
        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);
        executive.setName("Star Health");
        InsurancePlan plan = new InsurancePlan();
        plan.setId(1);
        plan.setExecutive(executive);

        PatientPolicy policy = new PatientPolicy();
        policy.setId(1);
        policy.setPatient(patient);
        policy.setPlan(plan);
        policy.setStartDate(LocalDate.now().minusDays(10));
        policy.setEndDate(LocalDate.now().minusDays(10).plusDays(365));
        return policy;
    }

    // helper: claim response (reimbursement)
    private ClaimResponseDto getResponseDto(Integer id, Double amount, ClaimStatus status) {
        return new ClaimResponseDto(id, 1, "John Doe", 1, 1, "Star Health",
                LocalDate.now(), amount, status, ClaimType.REIMBURSEMENT,
                null, null, null, null, null, 1, "Star Health", null, null, null);
    }
    @Test
    public void insertClaimTest() {
        Patient patient = getPatient(1, "john");
        PatientPolicy policy = getPolicy(patient);

        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);
        executive.setName("Star Health");

        ClaimDto claimDto = new ClaimDto(1, 1, 1, LocalDate.now(), 5000.0);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientPolicyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(insuranceExecutiveRepository.findById(1)).thenReturn(Optional.of(executive));
        // return the same claim object the service saves
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponseDto result = claimService.insertClaim(claimDto, "john");

        assertEquals(ClaimStatus.SUBMITTED, result.status());
        assertEquals(ClaimType.REIMBURSEMENT, result.claimType());
        assertEquals(5000.0, result.claimAmount());

        verify(patientRepository, times(1)).findById(1);
        verify(patientPolicyRepository, times(1)).findById(1);
        verify(insuranceExecutiveRepository, times(1)).findById(1);
        verify(claimRepository, times(1)).save(any(Claim.class));

        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(patientRepository);
    }

    // "mary" tries to submit a claim for john (not her own), so it is blocked
    @Test
    public void insertClaimTest_Ownership() {
        Patient patient = getPatient(1, "john");
        ClaimDto claimDto = new ClaimDto(1, 1, 1, LocalDate.now(), 5000.0);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        // logged-in patient "mary" tries to submit claim for john
        assertThrows(OwnershipException.class,
                () -> claimService.insertClaim(claimDto, "mary"));

        verify(patientRepository, times(1)).findById(1);

        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(patientPolicyRepository);
        verifyNoMoreInteractions(insuranceExecutiveRepository);
        verifyNoMoreInteractions(claimRepository);
    }

    // claim date is before the policy started, so the claim is rejected
    @Test
    public void insertClaimTest_InvalidDate() {
        Patient patient = getPatient(1, "john");

        // policy starts after 5 days -> today is outside the policy period
        PatientPolicy policy = new PatientPolicy();
        policy.setId(1);
        policy.setPatient(patient);
        policy.setStartDate(LocalDate.now().plusDays(5));
        policy.setEndDate(LocalDate.now().plusDays(5).plusDays(365));

        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);

        ClaimDto claimDto = new ClaimDto(1, 1, 1, LocalDate.now(), 5000.0);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientPolicyRepository.findById(1)).thenReturn(Optional.of(policy));
        when(insuranceExecutiveRepository.findById(1)).thenReturn(Optional.of(executive));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.insertClaim(claimDto, "john"));

        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
    }

    // patient 1 exists and has 2 claims, so 2 results come back
    @Test
    public void getClaimsByPatientIdTest() {
        ClaimResponseDto dto1 = getResponseDto(1, 5000.0, ClaimStatus.SUBMITTED);
        ClaimResponseDto dto2 = getResponseDto(2, 8000.0, ClaimStatus.APPROVED);

        when(patientRepository.existsById(1)).thenReturn(true);
        when(claimRepository.getClaimsByPatientId(1, PageRequest.of(0, 5)))
                .thenReturn(List.of(dto1, dto2));

        assertEquals(2, claimService.getClaimsByPatientId(1, 0, 5).size());

        verify(patientRepository, times(1)).existsById(1);
        verify(claimRepository, times(1)).getClaimsByPatientId(1, PageRequest.of(0, 5));

        verifyNoMoreInteractions(patientRepository);
        verifyNoMoreInteractions(claimRepository);
    }
    // v2: logged-in patient "john" has 2 claims, so 2 results come back
    @Test
    public void getMyClaimsTest() {
        ClaimResponseDto dto1 = getResponseDto(1, 5000.0, ClaimStatus.SUBMITTED);
        ClaimResponseDto dto2 = getResponseDto(2, 8000.0, ClaimStatus.APPROVED);

        when(claimRepository.getClaimsByPatientUsername("john", PageRequest.of(0, 5)))
                .thenReturn(List.of(dto1, dto2));

        assertEquals(2, claimService.getMyClaims("john", 0, 5).size());

        verify(claimRepository, times(1)).getClaimsByPatientUsername("john", PageRequest.of(0, 5));
        verifyNoMoreInteractions(claimRepository);
    }

    // insurance company 1 filters its SUBMITTED reimbursement claims -> 2 claims on page 0 with pagination info
    @Test
    public void filterClaimsTest() {
        ClaimResponseDto dto1 = getResponseDto(1, 5000.0, ClaimStatus.SUBMITTED);
        ClaimResponseDto dto2 = getResponseDto(2, 8000.0, ClaimStatus.SUBMITTED);

        when(claimRepository.filterClaims(null, null, 1, ClaimStatus.SUBMITTED, null, null, null, null,
                ClaimType.REIMBURSEMENT, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        ClaimFilterDto result = claimService.filterClaims(null, null, 1, ClaimStatus.SUBMITTED, null, null,
                null, null, ClaimType.REIMBURSEMENT, 0, 5);

        assertEquals(2, result.list().size());
        assertEquals(1, result.paginationInfo().totalPages());
        assertEquals(2, result.paginationInfo().totalRecords());

        verify(claimRepository, times(1)).filterClaims(null, null, 1, ClaimStatus.SUBMITTED, null, null,
                null, null, ClaimType.REIMBURSEMENT, PageRequest.of(0, 5));
        verifyNoMoreInteractions(claimRepository);
    }

    // insurance company verifies documents, types 4500 and approves a SUBMITTED reimbursement claim
    @Test
    public void updateClaimStatusTest() {
        Claim claim = new Claim();
        claim.setId(1);
        claim.setClaimType(ClaimType.REIMBURSEMENT);
        claim.setClaimAmount(5000.0);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(claim)).thenReturn(claim);

        ClaimResponseDto result = claimService.updateClaimStatus(1, ClaimStatus.APPROVED, 4500.0);

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        assertEquals(4500.0, claim.getApprovedAmount());
        assertEquals(LocalDate.now(), claim.getApprovedDate());
        assertEquals(ClaimStatus.APPROVED, result.status());

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(1)).save(claim);
        verifyNoMoreInteractions(claimRepository);
    }

    // claim is already REJECTED, so it cannot be approved again
    @Test
    public void updateClaimStatusTest_AlreadyDecided() {
        Claim claim = new Claim();
        claim.setId(1);
        claim.setClaimType(ClaimType.REIMBURSEMENT);
        claim.setStatus(ClaimStatus.REJECTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.updateClaimStatus(1, ClaimStatus.APPROVED, 4500.0));

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
    }

    // insurance company types 6000 but the patient claimed only 5000, so it is not allowed
    @Test
    public void updateClaimStatusTest_AmountTooHigh() {
        Claim claim = new Claim();
        claim.setId(1);
        claim.setClaimType(ClaimType.REIMBURSEMENT);
        claim.setClaimAmount(5000.0);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.updateClaimStatus(1, ClaimStatus.APPROVED, 6000.0));

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
    }

    // ---------------- CASHLESS (hospital -> TPA -> insurance company) ----------------

    // helper: cashless claim created at TPA initial approval
    // patient "john", hospital "apollo", tpa "mediassist"
    private Claim getCashlessClaim(ClaimStatus status) {
        Patient patient = getPatient(1, "john");

        HealthcareProvider provider = new HealthcareProvider();
        provider.setId(1);
        provider.setName("Apollo Hospital");
        provider.setUser(new User("apollo", "encodedPassword", Role.HEALTHCARE_PROVIDER));

        Tpa tpa = new Tpa();
        tpa.setId(1);
        tpa.setName("Medi Assist");
        tpa.setUser(new User("mediassist", "encodedPassword", Role.TPA));

        PreAuthorization preAuthorization = new PreAuthorization();
        preAuthorization.setId(1);
        preAuthorization.setHealthcareProvider(provider);

        Claim claim = new Claim();
        claim.setId(1);
        claim.setPatient(patient);
        claim.setPolicy(getPolicy(patient));
        claim.setTpa(tpa);
        claim.setPreAuthorization(preAuthorization);
        claim.setClaimType(ClaimType.CASHLESS);
        claim.setClaimAmount(60000.0);
        claim.setStatus(status);
        return claim;
    }

    // hospital "apollo" submits final bill amount for an INITIAL_APPROVED claim, so it goes to TPA as SUBMITTED
    @Test
    public void submitFinalBillTest() {
        Claim claim = getCashlessClaim(ClaimStatus.INITIAL_APPROVED);
        CashlessClaimDto dto = new CashlessClaimDto(58000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(claim)).thenReturn(claim);

        ClaimResponseDto result = claimService.submitFinalBill(1, dto, "apollo");

        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertEquals(58000.0, claim.getClaimAmount());
        assertEquals(ClaimStatus.SUBMITTED, result.status());

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(1)).save(claim);
        // TPA gets a notification
        verify(notificationService, times(1)).add(eq(claim.getTpa().getUser()), anyString());
        verifyNoMoreInteractions(claimRepository);
    }

    // final bill was already submitted (status SUBMITTED), so it cannot be sent again
    @Test
    public void submitFinalBillTest_AlreadySubmitted() {
        Claim claim = getCashlessClaim(ClaimStatus.SUBMITTED);
        CashlessClaimDto dto = new CashlessClaimDto(58000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.submitFinalBill(1, dto, "apollo"));

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
    }

    // another hospital "fortis" tries to submit the final bill on apollo's claim, so it is blocked
    @Test
    public void submitFinalBillTest_Ownership() {
        Claim claim = getCashlessClaim(ClaimStatus.INITIAL_APPROVED);
        CashlessClaimDto dto = new CashlessClaimDto(58000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(OwnershipException.class,
                () -> claimService.submitFinalBill(1, dto, "fortis"));

        verify(claimRepository, times(1)).findById(1);
        verifyNoMoreInteractions(claimRepository);
    }

    // TPA "mediassist" gives final approval of 55000 and sends the claim to insurance executive 1
    @Test
    public void finalApprovalTest() {
        Claim claim = getCashlessClaim(ClaimStatus.SUBMITTED);
        claim.setClaimAmount(58000.0);

        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);
        executive.setName("Star Health");

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(insuranceExecutiveRepository.findById(1)).thenReturn(Optional.of(executive));
        when(claimRepository.save(claim)).thenReturn(claim);

        ClaimResponseDto result = claimService.finalApproval(1, ClaimStatus.APPROVED, 55000.0, 1, "mediassist");

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        assertEquals(55000.0, claim.getApprovedAmount());
        assertEquals(executive, claim.getExecutive());
        assertEquals(55000.0, result.approvedAmount());
        assertEquals(LocalDate.now(), result.approvedDate());

        verify(claimRepository, times(1)).findById(1);
        verify(insuranceExecutiveRepository, times(1)).findById(1);
        verify(claimRepository, times(1)).save(claim);
        // hospital and insurance company get notifications
        verify(notificationService, times(2)).add(any(), anyString());
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(insuranceExecutiveRepository);
    }

    // TPA tries to approve more than the bill amount, so it is not allowed
    @Test
    public void finalApprovalTest_AmountTooHigh() {
        Claim claim = getCashlessClaim(ClaimStatus.SUBMITTED);
        claim.setClaimAmount(58000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.finalApproval(1, ClaimStatus.APPROVED, 70000.0, 1, "mediassist"));

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(insuranceExecutiveRepository);
    }

    // insurance company tries to APPROVE a cashless claim (TPA approves cashless claims, insurance only pays / rejects)
    @Test
    public void updateClaimStatusTest_Cashless() {
        Claim claim = getCashlessClaim(ClaimStatus.APPROVED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimService.updateClaimStatus(1, ClaimStatus.APPROVED, 55000.0));

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(0)).save(any(Claim.class));
        verifyNoMoreInteractions(claimRepository);
    }

    // insurance company REJECTS a cashless claim that TPA approved, so status becomes REJECTED
    @Test
    public void updateClaimStatusTest_CashlessReject() {
        Claim claim = getCashlessClaim(ClaimStatus.APPROVED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimRepository.save(claim)).thenReturn(claim);

        ClaimResponseDto result = claimService.updateClaimStatus(1, ClaimStatus.REJECTED, null);

        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertEquals(ClaimStatus.REJECTED, result.status());

        verify(claimRepository, times(1)).findById(1);
        verify(claimRepository, times(1)).save(claim);
        // hospital gets a notification
        verify(notificationService, times(1)).add(eq(claim.getPreAuthorization().getHealthcareProvider().getUser()), anyString());
        verifyNoMoreInteractions(claimRepository);
    }

    @Test
    public void getClaimStatsTest() {
        when(claimRepository.getClaimCountByStatus()).thenReturn(List.of(
                new ClaimStatusCountDto(ClaimStatus.PAID, 4L),
                new ClaimStatusCountDto(ClaimStatus.REJECTED, 2L)));

        ChartDto result = claimService.getClaimStats();

        assertEquals(List.of("PAID", "REJECTED"), result.labels());
        assertEquals(List.of(4L, 2L), result.values());

        verify(claimRepository, times(1)).getClaimCountByStatus();
        verifyNoMoreInteractions(claimRepository);
    }
}