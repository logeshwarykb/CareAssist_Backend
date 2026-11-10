package com.springboot.medical_billing.dto.response;

public record TpaResponseDto(
        Integer id,
        Integer userId,
        String username,
        String name,
        String contactNumber,
        String email
) {
}
