package com.springboot.medical_billing.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.medical_billing.dto.request.InsurancePlanDto;
import com.springboot.medical_billing.dto.response.InsurancePlanFilterDto;
import com.springboot.medical_billing.dto.response.InsurancePlanResponseDto;
import com.springboot.medical_billing.dto.response.PaginationDto;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.InsurancePlanMapper;
import com.springboot.medical_billing.model.InsuranceExecutive;
import com.springboot.medical_billing.model.InsurancePlan;
import com.springboot.medical_billing.repository.InsuranceExecutiveRepository;
import com.springboot.medical_billing.repository.InsurancePlanRepository;
import com.springboot.medical_billing.repository.PatientPolicyRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class InsurancePlanService {

    private final InsurancePlanRepository insurancePlanRepository;
    private final InsurancePlanMapper insurancePlanMapper;
    private final InsuranceExecutiveRepository executiveRepository;
    private final PatientPolicyRepository patientPolicyRepository;

    Logger logger = LoggerFactory.getLogger(InsurancePlanService.class);

    // logged-in insurance company adds a plan (plan is linked to that company)
    public InsurancePlanResponseDto insertInsurancePlan(
            InsurancePlanDto dto, String executiveUsername) {

        // Step 1: Fetch the logged-in insurance company
        InsuranceExecutive executive = executiveRepository.getByUsername(executiveUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        // Step 2: Plan name must be unique
        if (insurancePlanRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException(
                    "An insurance plan with this name already exists");
        }

        // Step 3: Map, attach company and save
        InsurancePlan insurancePlan = insurancePlanMapper.convertDtoToEntity(dto);
        insurancePlan.setExecutive(executive);
        InsurancePlan savedPlan = insurancePlanRepository.save(insurancePlan);
        logger.info("Insurance plan {} added by {}", dto.name(), executiveUsername);

        return insurancePlanMapper.convertEntityToDto(savedPlan);
    }

    // logged-in insurance company adds many plans at once (all plans are saved or none)
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertInsurancePlans(List<InsurancePlanDto> list, String executiveUsername) {

        // Step 1: Fetch the logged-in insurance company
        InsuranceExecutive executive = executiveRepository.getByUsername(executiveUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company not found"));

        // Step 2: Map each plan and attach company (plan name must be unique)
        List<InsurancePlan> plans = new ArrayList<>();
        for (InsurancePlanDto dto : list) {
            if (insurancePlanRepository.findByName(dto.name()).isPresent()) {
                throw new IllegalArgumentException("An insurance plan with name " + dto.name() + " already exists");
            }
            InsurancePlan insurancePlan = insurancePlanMapper.convertDtoToEntity(dto);
            insurancePlan.setExecutive(executive);
            plans.add(insurancePlan);
        }

        // Step 3: Save all plans in one go
        insurancePlanRepository.saveAll(plans);
        logger.info("{} insurance plans added in batch by {}", plans.size(), executiveUsername);
    }

    public InsurancePlanResponseDto getById(Integer id) {
        InsurancePlan insurancePlan = insurancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found with ID: " + id));
        return insurancePlanMapper.convertEntityToDto(insurancePlan);
    }

    public List<InsurancePlanResponseDto> getAllInsurancePlans(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return insurancePlanRepository.getAllInsurancePlans(pageable);
    }

    // search by plan name + pagination
    // executiveUsername = null -> plans of all companies (patient), else only that company's plans
    public InsurancePlanFilterDto searchPlans(String executiveUsername, String name, Integer page, Integer size) {

        // empty search box -> no name filter
        if (name != null && name.isBlank()) {
            name = null;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<InsurancePlan> planPage = insurancePlanRepository.searchPlans(executiveUsername, name, pageable);

        List<InsurancePlanResponseDto> list = planPage.getContent()
                .stream()
                .map(insurancePlanMapper::convertEntityToDto)
                .toList();

        return new InsurancePlanFilterDto(
                list,
                new PaginationDto(planPage.getTotalPages(), planPage.getTotalElements(), planPage.getNumberOfElements())
        );
    }

    // insurance company edits its own plan
    public InsurancePlanResponseDto updateInsurancePlan(
            Integer id, InsurancePlanDto dto, String executiveUsername) {

        // Step 1: Fetch plan
        InsurancePlan insurancePlan = insurancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found with ID: " + id));

        // Step 2: Only the company that created the plan can edit it
        checkOwnership(insurancePlan, executiveUsername);

        // Step 3: Update and save
        insurancePlan.setName(dto.name());
        insurancePlan.setCoverageAmount(dto.coverageAmount());
        insurancePlan.setPremiumAmount(dto.premiumAmount());
        insurancePlan.setCoverageDetails(dto.coverageDetails());
        insurancePlan.setTermsConditions(dto.termsConditions());
        logger.info("Insurance plan {} updated by {}", id, executiveUsername);

        return insurancePlanMapper.convertEntityToDto(insurancePlanRepository.save(insurancePlan));
    }

    // insurance company deletes its own plan
    public void deleteInsurancePlan(Integer id, String executiveUsername) {

        // Step 1: Fetch plan
        InsurancePlan insurancePlan = insurancePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance plan not found with ID: " + id));

        // Step 2: Only the company that created the plan can delete it
        checkOwnership(insurancePlan, executiveUsername);

        // Step 3: Plan already taken by patients cannot be deleted
        if (patientPolicyRepository.isPlanSelected(id)) {
            throw new IllegalArgumentException("Plan is already selected by patients, it cannot be deleted");
        }

        // Step 4: Delete
        insurancePlanRepository.delete(insurancePlan);
        logger.info("Insurance plan {} deleted by {}", id, executiveUsername);
    }

    private void checkOwnership(InsurancePlan insurancePlan, String executiveUsername) {
        if (!insurancePlan.getExecutive().getUser().getUsername().equals(executiveUsername)) {
            throw new OwnershipException("Operation not allowed! You can change only your own plans");
        }
    }

    public InsurancePlanFilterDto filterInsurancePlans(
            String name,
            Double minCoverage,
            Double maxCoverage,
            Double minPremium,
            Double maxPremium,
            Integer page,
            Integer size) {

        if (minCoverage != null && maxCoverage != null
                && minCoverage > maxCoverage) {
            throw new IllegalArgumentException("Minimum coverage cannot exceed maximum coverage");
        }

        // empty search box -> no name filter
        if (name != null && name.isBlank()) {
            name = null;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<InsurancePlanResponseDto> pageDto = insurancePlanRepository.filterInsurancePlans(
                name,
                minCoverage,
                maxCoverage,
                minPremium,
                maxPremium,
                pageable);

        return new InsurancePlanFilterDto(
                pageDto.getContent(),
                new PaginationDto(pageDto.getTotalPages(), pageDto.getTotalElements(), pageDto.getNumberOfElements())
        );
    }

}