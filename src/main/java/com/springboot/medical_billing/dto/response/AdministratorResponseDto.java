package com.springboot.medical_billing.dto.response;

public record AdministratorResponseDto(
        Integer id,
        Integer userId,
        String name
) {
}