package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PatientPolicyDto;
import com.springboot.medical_billing.dto.response.PatientPolicyResponseDto;
import com.springboot.medical_billing.enums.PolicyStatus;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.PatientPolicyMapper;
import com.springboot.medical_billing.model.InsurancePlan;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.PatientPolicy;
import com.springboot.medical_billing.repository.InsurancePlanRepository;
import com.springboot.medical_billing.repository.PatientPolicyRepository;
import com.springboot.medical_billing.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientPolicyService {

    private final PatientPolicyRepository patientPolicyRepository;
    private final PatientRepository patientRepository;
    private final InsurancePlanRepository insurancePlanRepository;

    Logger logger = LoggerFactory.getLogger(PatientPolicyService.class);



    public PatientPolicyResponseDto insertPatientPolicy(PatientPolicyDto patientPolicyDto) {
        if (patientPolicyDto.endDate()
                .isBefore(patientPolicyDto.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        // a policy cannot start on a past date
        if (patientPolicyDto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Policy cannot start in the past");
        }

        // policy period is exactly 1 year (365 days)
        if (!patientPolicyDto.endDate().equals(patientPolicyDto.startDate().plusDays(365))) {
            throw new IllegalArgumentException("End date must be 365 days after the start date");
        }
        Patient patient = patientRepository.findById(patientPolicyDto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        InsurancePlan plan = insurancePlanRepository.findById(patientPolicyDto.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found"));

        PatientPolicy patientPolicy = new PatientPolicy();
        patientPolicy.setPatient(patient);
        patientPolicy.setPlan(plan);
        patientPolicy.setStartDate(patientPolicyDto.startDate());
        patientPolicy.setEndDate(patientPolicyDto.endDate());
        patientPolicy.setStatus(PolicyStatus.ACTIVE);
        logger.info("Patient {} selected insurance plan {}", patient.getId(), plan.getName());
        return PatientPolicyMapper.convertEntityToDto(
                patientPolicyRepository.save(patientPolicy));
    }

    public List<PatientPolicyResponseDto> getAllPatientPolicies(
            Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return patientPolicyRepository.getAllPatientPolicies(pageable);
    }

    public PatientPolicyResponseDto getById(Integer id) {
        PatientPolicy patientPolicy = patientPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient policy not found"));
        return PatientPolicyMapper.convertEntityToDto(patientPolicy);
    }

    public List<PatientPolicyResponseDto> getPoliciesByPatientId(
            Integer patientId, Integer page, Integer size) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found");
        }
        Pageable pageable = PageRequest.of(page, size);
        return patientPolicyRepository.getPoliciesByPatientId(patientId, pageable);
    }
}
