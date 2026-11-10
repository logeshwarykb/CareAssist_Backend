package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.ClaimPaymentDto;
import com.springboot.medical_billing.dto.response.ClaimPaymentResponseDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.mapper.ClaimPaymentMapper;
import com.springboot.medical_billing.model.Claim;
import com.springboot.medical_billing.model.ClaimPayment;
import com.springboot.medical_billing.repository.ClaimPaymentRepository;
import com.springboot.medical_billing.repository.ClaimRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimPaymentService {

    private final ClaimPaymentRepository claimPaymentRepository;
    private final ClaimRepository claimRepository;
    private final ClaimPaymentMapper claimPaymentMapper;
    private final NotificationService notificationService;

    Logger logger = LoggerFactory.getLogger(ClaimPaymentService.class);

    public ClaimPaymentResponseDto addClaimPayment(ClaimPaymentDto claimPaymentDto) {
        // Step 1: Fetch claim
        Claim claim = claimRepository.findById(claimPaymentDto.claimId())
                .orElseThrow(() -> new EntityNotFoundException("Claim ID not found"));

        // Step 2: Only approved claims can be paid, not more than the approved amount
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved claims can be paid");
        }
        if (claimPaymentDto.amount() > claim.getApprovedAmount()) {
            throw new IllegalArgumentException("Payment amount cannot be more than the approved amount");
        }

        // Step 3: Save payment and mark claim as PAID
        ClaimPayment claimPayment = claimPaymentMapper.convertDtoToEntity(claimPaymentDto);
        claimPayment.setClaim(claim);
        ClaimPayment savedClaimPayment = claimPaymentRepository.save(claimPayment);
        claim.setStatus(ClaimStatus.PAID);
        claimRepository.save(claim);

        // Step 4: Notify patient (and hospital for cashless)
        if (claim.getClaimType() == ClaimType.REIMBURSEMENT) {
            notificationService.add(claim.getPatient().getUser(),
                    "Claim " + claim.getId() + " approved - Rs." + claimPaymentDto.amount() + " paid by " + claim.getExecutive().getName());
        } else {
            notificationService.add(claim.getPatient().getUser(),
                    "Cashless Claim " + claim.getId() + " settled - Rs." + claimPaymentDto.amount() + " paid to the hospital by " + claim.getExecutive().getName());
            notificationService.add(claim.getPreAuthorization().getHealthcareProvider().getUser(),
                    "Rs." + claimPaymentDto.amount() + " received from " + claim.getExecutive().getName() + " for Claim " + claim.getId());
        }
        logger.info("Payment of Rs.{} done for claim {}", claimPaymentDto.amount(), claim.getId());

        return claimPaymentMapper.convertEntityToDto(savedClaimPayment);
    }

    public ClaimPaymentResponseDto getClaimPaymentById(Integer id) {
        ClaimPayment claimPayment = claimPaymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Claim payment ID not found"));
        return claimPaymentMapper.convertEntityToDto(claimPayment);
    }

    public List<ClaimPaymentResponseDto> getAllClaimPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimPaymentRepository.findAll(pageable)
                .stream()
                .map(claimPaymentMapper::convertEntityToDto)
                .toList();
    }

    public List<ClaimPaymentResponseDto> getClaimPaymentsByClaimId(
            Integer claimId, int page, int size) {
        if (!claimRepository.existsById(claimId)) {
            throw new EntityNotFoundException("Claim ID not found");
        }
        Pageable pageable = PageRequest.of(page, size);
        return claimPaymentRepository.getClaimPaymentsByClaimId(claimId, pageable)
                .stream()
                .map(claimPaymentMapper::convertEntityToDto)
                .toList();
    }
}