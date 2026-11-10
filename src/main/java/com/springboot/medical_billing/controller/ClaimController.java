package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.CashlessClaimDto;
import com.springboot.medical_billing.dto.request.ClaimDto;
import com.springboot.medical_billing.dto.response.ChartDto;
import com.springboot.medical_billing.dto.response.ClaimFilterDto;
import com.springboot.medical_billing.dto.response.ClaimResponseDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimController {

    private final ClaimService claimService;

    // REIMBURSEMENT: patient submits the claim
    @PostMapping("/api/claim/add")
    public ClaimResponseDto insertClaim(@Valid @RequestBody ClaimDto claimDto, Principal principal) {
        return claimService.insertClaim(claimDto, principal.getName());
    }

    // CASHLESS: hospital submits the final bill against the claim ID got at initial approval
    @PutMapping("/api/claim/cashless/final-bill/{claimId}")
    public ClaimResponseDto submitFinalBill(@PathVariable Integer claimId,
                                            @Valid @RequestBody CashlessClaimDto dto,
                                            Principal principal) {
        return claimService.submitFinalBill(claimId, dto, principal.getName());
    }

    // CASHLESS: TPA gives final approval with approved amount and sends the claim to insurance company
    @PutMapping("/api/claim/tpa/final-approval/{claimId}")
    public ClaimResponseDto finalApproval(@PathVariable Integer claimId,
                                          @RequestParam("status") ClaimStatus status,
                                          @RequestParam(value = "approvedAmount", required = false) Double approvedAmount,
                                          @RequestParam(value = "executiveId", required = false) Integer executiveId,
                                          Principal principal) {
        return claimService.finalApproval(claimId, status, approvedAmount, executiveId, principal.getName());
    }

    @GetMapping("/api/claim/tpa/{tpaId}")
    public List<ClaimResponseDto> getClaimsByTpaId(
            @PathVariable Integer tpaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getClaimsByTpaId(tpaId, page, size);
    }

    @GetMapping("/api/claim/provider/{providerId}")
    public List<ClaimResponseDto> getClaimsByProviderId(
            @PathVariable Integer providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getClaimsByProviderId(providerId, page, size);
    }

    @GetMapping("/api/claim/{id}")
    public ClaimResponseDto getById(@PathVariable Integer id) {
        return claimService.getById(id);
    }

    @GetMapping("/api/claim/all")
    public List<ClaimResponseDto> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getAllClaims(page, size);
    }

    @GetMapping("/api/claim/patient/v1/{patientId}")
    public List<ClaimResponseDto> getClaimsByPatientId(
            @PathVariable Integer patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getClaimsByPatientId(patientId, page, size);
    }

    @GetMapping("/api/claim/patient/v2")
    public List<ClaimResponseDto> getMyClaims(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getMyClaims(principal.getName(), page, size);
    }

    @GetMapping("/api/claim/executive/{executiveId}")
    public List<ClaimResponseDto> getClaimsByExecutiveId(
            @PathVariable Integer executiveId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimService.getClaimsByExecutiveId(executiveId, page, size);
    }

    @PutMapping("/api/claim/status/{claimId}")
    public ClaimResponseDto updateClaimStatus(@PathVariable Integer claimId,
                                              @RequestParam("status") ClaimStatus status,
                                              @RequestParam(value = "approvedAmount", required = false) Double approvedAmount) {
        return claimService.updateClaimStatus(claimId, status, approvedAmount);
    }

    @GetMapping("/api/claim/filter")
    public ClaimFilterDto filterClaims(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer policyId,
            @RequestParam(required = false) Integer executiveId,
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) ClaimType claimType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return claimService.filterClaims(
                patientId,
                policyId,
                executiveId,
                status,
                fromDate,
                toDate,
                minAmount,
                maxAmount,
                claimType,
                page,
                size);
    }

    @PostMapping("/api/claim/upload/{claimId}")
    public void upload(Principal principal,
                       @PathVariable Integer claimId,
                       @RequestParam("invoice") MultipartFile invoice,
                       @RequestParam("medical") MultipartFile medical) throws IOException {
        String username = principal.getName();
        claimService.upload(username, claimId, invoice, medical);
    }
    @DeleteMapping("/api/claim/hard/{claimId}")
    public void deleteClaim(Principal principal, @PathVariable Integer claimId) {
        claimService.deleteClaim(principal.getName(), claimId);
    }

    @GetMapping("/api/claim/status/stat")
    public ChartDto getClaimStats() {
        return claimService.getClaimStats();
    }

}