package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.InsurancePlanDto;
import com.springboot.medical_billing.dto.response.InsurancePlanResponseDto;
import com.springboot.medical_billing.model.InsurancePlan;
import org.springframework.stereotype.Component;

@Component
public class InsurancePlanMapper {

    public InsurancePlan convertDtoToEntity(InsurancePlanDto dto) {
        InsurancePlan insurancePlan = new InsurancePlan();
        insurancePlan.setName(dto.name());
        insurancePlan.setCoverageAmount(dto.coverageAmount());
        insurancePlan.setPremiumAmount(dto.premiumAmount());
        insurancePlan.setCoverageDetails(dto.coverageDetails());
        insurancePlan.setTermsConditions(dto.termsConditions());
        return insurancePlan;
    }

    public InsurancePlanResponseDto convertEntityToDto(InsurancePlan insurancePlan) {
        return new InsurancePlanResponseDto(
                insurancePlan.getId(),
                insurancePlan.getName(),
                insurancePlan.getCoverageAmount(),
                insurancePlan.getPremiumAmount(),
                insurancePlan.getCoverageDetails(),
                insurancePlan.getTermsConditions(),
                insurancePlan.getExecutive() == null ? null : insurancePlan.getExecutive().getId(),
                insurancePlan.getExecutive() == null ? null : insurancePlan.getExecutive().getName()
        );
    }
}