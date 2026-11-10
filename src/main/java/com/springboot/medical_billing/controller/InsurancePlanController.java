package com.springboot.medical_billing.controller;
import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.springboot.medical_billing.dto.request.InsurancePlanDto;
import com.springboot.medical_billing.dto.response.InsurancePlanFilterDto;
import com.springboot.medical_billing.dto.response.InsurancePlanResponseDto;
import com.springboot.medical_billing.service.InsurancePlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InsurancePlanController {

    private final InsurancePlanService insurancePlanService;

    @PostMapping("/api/insurance-plan/add")
    public InsurancePlanResponseDto insertInsurancePlan(
            @Valid @RequestBody InsurancePlanDto dto, Principal principal) {
        return insurancePlanService.insertInsurancePlan(dto, principal.getName());
    }

    // batch insert: add many plans at once
    @PostMapping("/api/insurance-plan/batch-add")
    public void batchInsertInsurancePlans(
            @RequestBody List<InsurancePlanDto> list, Principal principal) {
        insurancePlanService.batchInsertInsurancePlans(list, principal.getName());
    }

    @GetMapping("/api/insurance-plan/my-plans")
    public InsurancePlanFilterDto getMyPlans(
            Principal principal,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size) {
        return insurancePlanService.searchPlans(principal.getName(), name, page, size);
    }

    @PutMapping("/api/insurance-plan/update/{id}")
    public InsurancePlanResponseDto updateInsurancePlan(
            @PathVariable Integer id,
            @Valid @RequestBody InsurancePlanDto dto,
            Principal principal) {
        return insurancePlanService.updateInsurancePlan(id, dto, principal.getName());
    }

    @DeleteMapping("/api/insurance-plan/delete/{id}")
    public void deleteInsurancePlan(@PathVariable Integer id, Principal principal) {
        insurancePlanService.deleteInsurancePlan(id, principal.getName());
    }

    @GetMapping("/api/insurance-plan/{id}")
    public InsurancePlanResponseDto getById(
            @PathVariable Integer id) {
        return insurancePlanService.getById(id);
    }

    @GetMapping("/api/insurance-plan/all")
    public List<InsurancePlanResponseDto> getAllInsurancePlans(
            @RequestParam("page") int page,
            @RequestParam("size") int size) {
        return insurancePlanService.getAllInsurancePlans(page, size);
    }

    @GetMapping("/api/insurance-plan/filter")
    public InsurancePlanFilterDto filterInsurancePlans(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "minCoverage", required = false) Double minCoverage,
            @RequestParam(value = "maxCoverage", required = false) Double maxCoverage,
            @RequestParam(value = "minPremium", required = false) Double minPremium,
            @RequestParam(value = "maxPremium", required = false) Double maxPremium,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size) {

        return insurancePlanService.filterInsurancePlans(
                name,
                minCoverage,
                maxCoverage,
                minPremium,
                maxPremium,
                page,
                size);
    }

}
