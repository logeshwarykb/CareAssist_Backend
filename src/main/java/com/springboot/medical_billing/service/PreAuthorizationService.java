package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PreAuthorizationDto;
import com.springboot.medical_billing.dto.response.PreAuthorizationResponseDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.enums.PolicyStatus;
import com.springboot.medical_billing.enums.PreAuthStatus;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.PreAuthorizationMapper;
import com.springboot.medical_billing.model.*;
import com.springboot.medical_billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PreAuthorizationService {

    private final PreAuthorizationRepository preAuthorizationRepository;
    private final PatientRepository patientRepository;
    private final PatientPolicyRepository patientPolicyRepository;
    private final HealthcareProviderRepository providerRepository;
    private final TpaRepository tpaRepository;
    private final ClaimRepository claimRepository;
    private final PreAuthorizationMapper preAuthorizationMapper;
    private final NotificationService notificationService;

    Logger logger = LoggerFactory.getLogger(PreAuthorizationService.class);

    // CASHLESS Step 1: Hospital sends pre-auth request (patient's policy card) to TPA
    public PreAuthorizationResponseDto insertPreAuthorization(
            PreAuthorizationDto dto, String providerUsername) {

        // Step 1: Fetch all related records
        HealthcareProvider provider = providerRepository.findById(dto.providerId())
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare provider not found"));

        // Is the logged-in provider sending the request in his/her own name?
        if (!(provider.getUser().getUsername().equals(providerUsername))) {
            throw new OwnershipException("Operation not allowed! You can send pre-auth request only as yourself");
        }

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        PatientPolicy policy = patientPolicyRepository.findById(dto.policyId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient policy not found"));

        Tpa tpa = tpaRepository.findById(dto.tpaId())
                .orElseThrow(() -> new ResourceNotFoundException("TPA not found"));

        // Step 2: Policy card must belong to this patient
        if (!policy.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Policy does not belong to the selected patient");
        }

        // Step 3: Policy must be active today
        LocalDate today = LocalDate.now();
        if (policy.getStatus() != PolicyStatus.ACTIVE
                || today.isBefore(policy.getStartDate())
                || today.isAfter(policy.getEndDate())) {
            throw new IllegalArgumentException("Policy is not active");
        }

        // Step 4: Map and attach related records
        PreAuthorization preAuthorization = preAuthorizationMapper.convertDtoToEntity(dto);
        preAuthorization.setPatient(patient);
        preAuthorization.setPolicy(policy);
        preAuthorization.setHealthcareProvider(provider);
        preAuthorization.setTpa(tpa);
        preAuthorization.setRequestDate(today);
        preAuthorization.setStatus(PreAuthStatus.PENDING);

        // Step 5: Save (no claim yet, so claimId is null)
        PreAuthorization savedPreAuth = preAuthorizationRepository.save(preAuthorization);

        // Step 6: Notify TPA
        notificationService.add(tpa.getUser(),
                "New pre-auth request from " + provider.getName() + " for " + patient.getFullName());
        logger.info("Pre-auth request sent to TPA {} for patient {} by {}", tpa.getName(), patient.getId(), providerUsername);
        return preAuthorizationMapper.convertEntityToDto(savedPreAuth, null);
    }

    // CASHLESS Step 2: TPA verifies the policy and gives INITIAL approval (or rejects).
    // On approval a claim is created and its claim ID is sent back to the hospital.
    @Transactional
    public PreAuthorizationResponseDto updatePreAuthStatus(
            Integer id, PreAuthStatus status, String tpaUsername) {

        // Step 1: Fetch pre-auth from DB
        PreAuthorization preAuthorization = preAuthorizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pre-authorization not found"));

        // Step 2: Only the TPA to whom the request was sent can decide
        if (!(preAuthorization.getTpa().getUser().getUsername().equals(tpaUsername))) {
            throw new OwnershipException("Operation not allowed! This request is not assigned to you");
        }

        // Step 3: TPA can only APPROVE or REJECT
        if (status != PreAuthStatus.APPROVED && status != PreAuthStatus.REJECTED) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        // Step 4: Already decided request cannot be changed again
        if (preAuthorization.getStatus() != PreAuthStatus.PENDING) {
            throw new IllegalArgumentException("Pre-authorization is already " + preAuthorization.getStatus());
        }

        // Step 5: Update status and save
        preAuthorization.setStatus(status);
        PreAuthorization updatedPreAuth = preAuthorizationRepository.save(preAuthorization);

        // Step 6: If rejected, no claim is created
        if (status == PreAuthStatus.REJECTED) {
            notificationService.add(preAuthorization.getHealthcareProvider().getUser(),
                    "Pre-auth request " + id + " rejected by TPA");
            notificationService.add(preAuthorization.getPatient().getUser(),
                    "Cashless request at " + preAuthorization.getHealthcareProvider().getName() + " rejected by TPA");
            logger.info("Pre-auth request {} rejected by TPA {}", id, tpaUsername);
            return preAuthorizationMapper.convertEntityToDto(updatedPreAuth, null);
        }

        // Step 7: Initial approval -> create cashless claim, hospital gets this claim ID
        Claim claim = new Claim();
        claim.setPatient(updatedPreAuth.getPatient());
        claim.setPolicy(updatedPreAuth.getPolicy());
        claim.setTpa(updatedPreAuth.getTpa());
        claim.setPreAuthorization(updatedPreAuth);
        claim.setClaimType(ClaimType.CASHLESS);
        claim.setClaimDate(LocalDate.now());
        claim.setClaimAmount(updatedPreAuth.getEstimatedAmount()); // replaced by final bill amount later
        claim.setStatus(ClaimStatus.INITIAL_APPROVED);
        Claim savedClaim = claimRepository.save(claim);

        // Step 8: Notify hospital and patient
        notificationService.add(preAuthorization.getHealthcareProvider().getUser(),
                "Pre-auth request " + id + " approved. Claim " + savedClaim.getId() + " created - please upload the final bill");
        notificationService.add(preAuthorization.getPatient().getUser(),
                "Cashless request at " + preAuthorization.getHealthcareProvider().getName() + " approved (Claim " + savedClaim.getId() + ")");
        logger.info("Pre-auth request {} approved by TPA {}, cashless claim {} created", id, tpaUsername, savedClaim.getId());

        return preAuthorizationMapper.convertEntityToDto(updatedPreAuth, savedClaim.getId());
    }

    public PreAuthorizationResponseDto getById(Integer id) {
        PreAuthorization preAuthorization = preAuthorizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pre-authorization not found"));
        return preAuthorizationMapper.convertEntityToDto(preAuthorization, getClaimId(id));
    }

    public List<PreAuthorizationResponseDto> getPreAuthorizationsByTpaId(
            Integer tpaId, int page, int size) {

        if (!tpaRepository.existsById(tpaId)) {
            throw new ResourceNotFoundException("TPA not found");
        }

        Pageable pageable = PageRequest.of(page, size);

        return preAuthorizationRepository.getPreAuthorizationsByTpaId(tpaId, pageable)
                .stream()
                .map(pa -> preAuthorizationMapper.convertEntityToDto(pa, getClaimId(pa.getId())))
                .toList();
    }

    public List<PreAuthorizationResponseDto> getPreAuthorizationsByProviderId(
            Integer providerId, int page, int size) {

        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Healthcare provider not found");
        }

        Pageable pageable = PageRequest.of(page, size);

        return preAuthorizationRepository.getPreAuthorizationsByProviderId(providerId, pageable)
                .stream()
                .map(pa -> preAuthorizationMapper.convertEntityToDto(pa, getClaimId(pa.getId())))
                .toList();
    }

    public List<PreAuthorizationResponseDto> getPreAuthorizationsByPatientId(
            Integer patientId, int page, int size) {

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found");
        }

        Pageable pageable = PageRequest.of(page, size);

        return preAuthorizationRepository.getPreAuthorizationsByPatientId(patientId, pageable)
                .stream()
                .map(pa -> preAuthorizationMapper.convertEntityToDto(pa, getClaimId(pa.getId())))
                .toList();
    }

    // v2: pre-auth requests of the logged-in patient
    public List<PreAuthorizationResponseDto> getMyPreAuthorizations(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return preAuthorizationRepository.getPreAuthorizationsByPatientUsername(username, pageable)
                .stream()
                .map(pa -> preAuthorizationMapper.convertEntityToDto(pa, getClaimId(pa.getId())))
                .toList();
    }

    // helper: claim ID created at initial approval (null if not approved yet)
    private Integer getClaimId(Integer preAuthorizationId) {
        return claimRepository.getByPreAuthorizationId(preAuthorizationId)
                .map(Claim::getId)
                .orElse(null);
    }
}
