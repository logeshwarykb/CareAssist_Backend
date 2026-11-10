package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.CashlessClaimDto;
import com.springboot.medical_billing.dto.request.ClaimDto;
import com.springboot.medical_billing.dto.response.ChartDto;
import com.springboot.medical_billing.dto.response.ClaimFilterDto;
import com.springboot.medical_billing.dto.response.ClaimResponseDto;
import com.springboot.medical_billing.dto.response.PaginationDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.mapper.ClaimMapper;
import com.springboot.medical_billing.model.Claim;
import com.springboot.medical_billing.model.InsuranceExecutive;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.PatientPolicy;
import com.springboot.medical_billing.repository.ClaimRepository;
import com.springboot.medical_billing.repository.InsuranceExecutiveRepository;
import com.springboot.medical_billing.repository.PatientPolicyRepository;
import com.springboot.medical_billing.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PatientRepository patientRepository;
    private final PatientPolicyRepository patientPolicyRepository;
    private final InsuranceExecutiveRepository insuranceExecutiveRepository;
    private final NotificationService notificationService;

    // REIMBURSEMENT: patient submits the claim
    public ClaimResponseDto insertClaim(ClaimDto claimDto, String patientUsername) {
        // Step 1: Fetch patient and check ownership
        Patient patient = patientRepository.findById(claimDto.patientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));
        if (!(patient.getUser().getUsername().equals(patientUsername))) {
            throw new OwnershipException("Operation not allowed! You can submit claim only for yourself");
        }

        // Step 2: Fetch policy and insurance company
        PatientPolicy policy = patientPolicyRepository.findById(claimDto.policyId())
                .orElseThrow(() -> new EntityNotFoundException("Patient policy not found"));
        InsuranceExecutive executive = insuranceExecutiveRepository.findById(claimDto.executiveId())
                .orElseThrow(() -> new EntityNotFoundException("Insurance executive not found"));

        // Step 3: Claim date must be inside the policy period
        if (claimDto.claimDate().isBefore(policy.getStartDate())
                || claimDto.claimDate().isAfter(policy.getEndDate())) {
            throw new IllegalArgumentException("Claim date is outside the policy period");
        }

        // Step 4: Claim must go to the company of the selected policy
        if (!policy.getPlan().getExecutive().getId().equals(executive.getId())) {
            throw new IllegalArgumentException("Selected policy does not belong to this insurance company");
        }

        // Step 5: Save claim
        Claim claim = ClaimMapper.convertDtoToEntity(claimDto);
        claim.setPatient(patient);
        claim.setPolicy(policy);
        claim.setExecutive(executive);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setClaimType(ClaimType.REIMBURSEMENT);
        Claim savedClaim = claimRepository.save(claim);
        logger.info("Reimbursement claim {} submitted by {}", savedClaim.getId(), patientUsername);
        return ClaimMapper.convertEntityToDto(savedClaim);
    }

    // CASHLESS: hospital sends the final bill amount to TPA
    public ClaimResponseDto submitFinalBill(Integer claimId, CashlessClaimDto dto, String providerUsername) {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        // Step 2: Only the hospital that raised the pre-auth
        if (!(claim.getPreAuthorization().getHealthcareProvider().getUser().getUsername().equals(providerUsername))) {
            throw new OwnershipException("Operation not allowed! You can submit final bill only for your own claim");
        }

        // Step 3: Final bill can be sent only once
        if (claim.getStatus() != ClaimStatus.INITIAL_APPROVED) {
            throw new IllegalArgumentException("Final bill already submitted");
        }

        // Step 4: Save amount and send to TPA
        claim.setClaimAmount(dto.claimAmount());
        claim.setClaimDate(LocalDate.now());
        claim.setStatus(ClaimStatus.SUBMITTED);
        Claim savedClaim = claimRepository.save(claim);

        logger.info("Final bill of Rs.{} submitted for claim {} by {}", dto.claimAmount(), claimId, providerUsername);

        // Step 5: Notify TPA
        notificationService.add(claim.getTpa().getUser(),
                "Final bill of Rs." + dto.claimAmount() + " received for Claim " + claimId);
        return ClaimMapper.convertEntityToDto(savedClaim);
    }

    // CASHLESS: TPA approves (with amount + insurance company) or rejects
    public ClaimResponseDto finalApproval(Integer claimId, ClaimStatus status, Double approvedAmount,
                                          Integer executiveId, String tpaUsername) {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        // Step 2: Only the TPA assigned to this claim
        if (!(claim.getTpa().getUser().getUsername().equals(tpaUsername))) {
            throw new OwnershipException("Operation not allowed! This claim is not assigned to you");
        }

        // Step 3: Approve -> amount cannot be more than the bill, send to insurance company
        if (status == ClaimStatus.APPROVED) {
            if (approvedAmount > claim.getClaimAmount()) {
                throw new IllegalArgumentException("Approved amount cannot be more than the claim amount");
            }
            InsuranceExecutive executive = insuranceExecutiveRepository.findById(executiveId)
                    .orElseThrow(() -> new EntityNotFoundException("Insurance executive not found"));
            // claim must go to the company of the patient's policy
            if (!claim.getPolicy().getPlan().getExecutive().getId().equals(executive.getId())) {
                throw new IllegalArgumentException("Selected policy does not belong to this insurance company");
            }
            claim.setExecutive(executive);
            claim.setApprovedAmount(approvedAmount);
            claim.setApprovedDate(LocalDate.now());
        }

        // Step 4: Update status and save
        claim.setStatus(status);
        Claim savedClaim = claimRepository.save(claim);

        // Step 5: Notify hospital (and insurance company if approved)
        if (status == ClaimStatus.APPROVED) {
            notificationService.add(claim.getPreAuthorization().getHealthcareProvider().getUser(),
                    "Claim " + claimId + " approved by TPA for Rs." + approvedAmount + " and sent to " + claim.getExecutive().getName());
            notificationService.add(claim.getExecutive().getUser(),
                    "Cashless Claim " + claimId + " approved by TPA for Rs." + approvedAmount + ". Please make the payment");
        } else {
            notificationService.add(claim.getPreAuthorization().getHealthcareProvider().getUser(),
                    "Final bill of Claim " + claimId + " rejected by TPA");
        }
        logger.info("Claim {} {} by TPA {}", claimId, status, tpaUsername);
        return ClaimMapper.convertEntityToDto(savedClaim);
    }

    public ClaimResponseDto getById(Integer id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));
        return ClaimMapper.convertEntityToDto(claim);
    }

    public List<ClaimResponseDto> getAllClaims(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.getAllClaims(pageable);
    }

    // v1: claims by patient ID
    public List<ClaimResponseDto> getClaimsByPatientId(Integer patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (!patientRepository.existsById(patientId)) {
            throw new EntityNotFoundException("Patient not found");
        }
        return claimRepository.getClaimsByPatientId(patientId, pageable);
    }

    // v2: claims of the logged-in patient
    public List<ClaimResponseDto> getMyClaims(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.getClaimsByPatientUsername(username, pageable);
    }

    public List<ClaimResponseDto> getClaimsByExecutiveId(Integer executiveId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.getClaimsByExecutiveId(executiveId, pageable);
    }

    public List<ClaimResponseDto> getClaimsByTpaId(Integer tpaId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.getClaimsByTpaId(tpaId, pageable);
    }

    public List<ClaimResponseDto> getClaimsByProviderId(Integer providerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.getClaimsByProviderId(providerId, pageable);
    }

    // Insurance company: REIMBURSEMENT approve (with amount) / reject, CASHLESS reject
    public ClaimResponseDto updateClaimStatus(Integer claimId, ClaimStatus status, Double approvedAmount) {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        // Step 2: Cashless claims are approved by TPA, not here
        if (claim.getClaimType() == ClaimType.CASHLESS && status == ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Cashless claims are approved by TPA");
        }

        // Step 3: Reimbursement claim already decided cannot be changed
        if (claim.getClaimType() == ClaimType.REIMBURSEMENT && claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new IllegalArgumentException("Claim is already " + claim.getStatus());
        }

        // Step 4: Approved amount typed by the insurance company
        if (status == ClaimStatus.APPROVED) {
            if (approvedAmount > claim.getClaimAmount()) {
                throw new IllegalArgumentException("Approved amount cannot be more than the claim amount");
            }
            claim.setApprovedAmount(approvedAmount);
            claim.setApprovedDate(LocalDate.now());
        }

        // Step 5: Update status and save
        claim.setStatus(status);
        Claim savedClaim = claimRepository.save(claim);

        // Step 6: Notify on rejection (approval is notified when payment is done)
        if (status == ClaimStatus.REJECTED) {
            if (claim.getClaimType() == ClaimType.REIMBURSEMENT) {
                notificationService.add(claim.getPatient().getUser(),
                        "Claim " + claimId + " rejected by the insurance company");
            } else {
                notificationService.add(claim.getPreAuthorization().getHealthcareProvider().getUser(),
                        "Cashless Claim " + claimId + " rejected by the insurance company");
            }
        }
        logger.info("Claim {} {} by insurance company", claimId, status);
        return ClaimMapper.convertEntityToDto(savedClaim);
    }

    public ClaimFilterDto filterClaims(
            Integer patientId,
            Integer policyId,
            Integer executiveId,
            ClaimStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Double minAmount,
            Double maxAmount,
            ClaimType claimType,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ClaimResponseDto> pageDto = claimRepository.filterClaims(
                patientId, policyId, executiveId,
                status, fromDate, toDate, minAmount, maxAmount, claimType, pageable);
        return new ClaimFilterDto(
                pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements())
        );
    }

    // documents were not uploaded -> patient's claim is removed
    public void deleteClaim(String username, Integer claimId) {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        // Step 2: Only the patient who submitted the claim
        if (!(claim.getPatient().getUser().getUsername().equals(username))) {
            throw new OwnershipException("Operation not allowed! You can delete only your own claim");
        }

        // Step 3: Claim with documents cannot be deleted
        if (claim.getInvoiceFile() != null) {
            throw new IllegalArgumentException("Submitted claims cannot be deleted");
        }

        claimRepository.delete(claim);
        logger.warn("Claim {} deleted because documents were not uploaded", claimId);
    }

    Logger logger = LoggerFactory.getLogger(ClaimService.class);
    private final static String uploadDir = "C:/Users/loges/OneDrive/Desktop/careAssist_Ui/careassist-ui/public/uploads/";

    private String saveFile(MultipartFile file) throws IOException {
        if(file == null || file.isEmpty())
            throw new NullPointerException("Please upload valid file");

        logger.info("uploaded file name {}", file.getOriginalFilename());
        logger.info("File size {}",file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        logger.info("File extension {}",  ext);

        // Original file name
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename() ;

        // file must stay inside the uploads folder (blocks names like ../../file.pdf)
        Path uploadPath = Paths.get(uploadDir).normalize();
        Path filePath = uploadPath.resolve(originalFileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file name");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return originalFileName;
    }

    // CASHLESS -> hospital uploads, REIMBURSEMENT -> patient uploads
    public void upload(String username, Integer claimId, MultipartFile invoice, MultipartFile medical) throws IOException {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found"));

        // Step 2: Only the owner can upload
        String ownerUsername = (claim.getClaimType() == ClaimType.CASHLESS)
                ? claim.getPreAuthorization().getHealthcareProvider().getUser().getUsername()
                : claim.getPatient().getUser().getUsername();
        if (!ownerUsername.equals(username)) {
            throw new OwnershipException("Operation not allowed! You can upload documents only for your own claim");
        }

        // Step 3: Save file names
        claim.setInvoiceFile(saveFile(invoice));
        claim.setMedicalFile(saveFile(medical));
        claimRepository.save(claim);
        logger.info("Documents uploaded for claim {} by {}", claimId, username);

        // Step 4: Reimbursement claim is now complete -> notify insurance company
        if (claim.getClaimType() == ClaimType.REIMBURSEMENT) {
            notificationService.add(claim.getExecutive().getUser(),
                    "New reimbursement Claim " + claimId + " from " + claim.getPatient().getFullName());
        }
    }
    public ChartDto getClaimStats() {
        return ClaimMapper.convertToChartDto(claimRepository.getClaimCountByStatus());
    }

}