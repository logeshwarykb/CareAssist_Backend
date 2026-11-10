package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.InsuranceExecutiveDto;
import com.springboot.medical_billing.dto.response.InsuranceExecutiveResponseDto;
import com.springboot.medical_billing.model.InsuranceExecutive;

public class InsuranceExecutiveMapper {

    public static InsuranceExecutive convertDtoToEntity(InsuranceExecutiveDto executiveDto) {
        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setName(executiveDto.name());
        executive.setContactNumber(executiveDto.contactNumber());
        executive.setEmail(executiveDto.email());
        return executive;
    }


    public static InsuranceExecutiveResponseDto convertEntityToDto(InsuranceExecutive executive) {
        return new InsuranceExecutiveResponseDto(
                executive.getId(),
                executive.getUser().getId(),
                executive.getUser().getUsername(),
                executive.getName(),
                executive.getContactNumber(),
                executive.getEmail()
        );
    }
}
