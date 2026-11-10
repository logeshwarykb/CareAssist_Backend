package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.PreAuthorizationDto;
import com.springboot.medical_billing.dto.response.PreAuthorizationResponseDto;
import com.springboot.medical_billing.enums.PreAuthStatus;
import com.springboot.medical_billing.service.PreAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PreAuthorizationController {

    private final PreAuthorizationService preAuthorizationService;

    // Hospital sends pre-auth request to TPA
    @PostMapping("/api/pre-auth/add")
    public PreAuthorizationResponseDto insertPreAuthorization(
            @Valid @RequestBody PreAuthorizationDto dto, Principal principal) {
        return preAuthorizationService.insertPreAuthorization(dto, principal.getName());
    }

    // TPA gives initial approval or rejects
    @PutMapping("/api/pre-auth/status/{id}")
    public PreAuthorizationResponseDto updatePreAuthStatus(
            @PathVariable Integer id,
            @RequestParam("status") PreAuthStatus status,
            Principal principal) {
        return preAuthorizationService.updatePreAuthStatus(id, status, principal.getName());
    }

    @GetMapping("/api/pre-auth/{id}")
    public PreAuthorizationResponseDto getById(@PathVariable Integer id) {
        return preAuthorizationService.getById(id);
    }

    @GetMapping("/api/pre-auth/tpa/{tpaId}")
    public List<PreAuthorizationResponseDto> getPreAuthorizationsByTpaId(
            @PathVariable Integer tpaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return preAuthorizationService.getPreAuthorizationsByTpaId(tpaId, page, size);
    }

    @GetMapping("/api/pre-auth/provider/{providerId}")
    public List<PreAuthorizationResponseDto> getPreAuthorizationsByProviderId(
            @PathVariable Integer providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return preAuthorizationService.getPreAuthorizationsByProviderId(providerId, page, size);
    }

    // v1: pre-auth requests by patient ID
    @GetMapping("/api/pre-auth/patient/v1/{patientId}")
    public List<PreAuthorizationResponseDto> getPreAuthorizationsByPatientId(
            @PathVariable Integer patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return preAuthorizationService.getPreAuthorizationsByPatientId(patientId, page, size);
    }

    // v2: pre-auth requests of the logged-in patient (username from token)
    @GetMapping("/api/pre-auth/patient/v2")
    public List<PreAuthorizationResponseDto> getMyPreAuthorizations(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return preAuthorizationService.getMyPreAuthorizations(principal.getName(), page, size);
    }
}
