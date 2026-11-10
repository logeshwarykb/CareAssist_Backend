
package com.springboot.medical_billing.dto.response;

public record InsuranceExecutiveResponseDto(
        Integer id,
        Integer userId,
        String username,
        String name,
        String contactNumber,
        String email
) {
}
