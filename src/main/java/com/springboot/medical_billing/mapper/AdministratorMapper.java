package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.AdministratorDto;
import com.springboot.medical_billing.dto.response.AdministratorResponseDto;
import com.springboot.medical_billing.model.Administrator;
import org.springframework.stereotype.Component;

@Component
public class AdministratorMapper {

    public Administrator convertDtoToEntity(AdministratorDto dto) {

        Administrator administrator = new Administrator();

        administrator.setName(dto.name());

        return administrator;
    }

    public AdministratorResponseDto convertEntityToDto(Administrator administrator) {

        return new AdministratorResponseDto(
                administrator.getId(),
                administrator.getUser().getId(),
                administrator.getName()
        );
    }
}