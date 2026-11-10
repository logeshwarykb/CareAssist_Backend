package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.PatientPolicyDto;
import com.springboot.medical_billing.dto.response.PatientPolicyResponseDto;
import com.springboot.medical_billing.service.PatientPolicyService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PatientPolicyController {

    private final PatientPolicyService patientPolicyService;

    @PostMapping("/api/patient-policy/add")
    public PatientPolicyResponseDto insertPatientPolicy(@Valid @RequestBody PatientPolicyDto patientPolicyDto) {
        return patientPolicyService.insertPatientPolicy(patientPolicyDto);
    }

    @GetMapping("/api/patient-policy/all")
    public List<PatientPolicyResponseDto> getAllPatientPolicies(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        return patientPolicyService.getAllPatientPolicies(page, size);
    }

    @GetMapping("/api/patient-policy/patient/{patientId}")
    public List<PatientPolicyResponseDto> getPoliciesByPatientId(
            @PathVariable Integer patientId,
            @RequestParam("page") int page,
            @RequestParam("size") int size) {

        return patientPolicyService.getPoliciesByPatientId(
                patientId, page, size);
    }


}