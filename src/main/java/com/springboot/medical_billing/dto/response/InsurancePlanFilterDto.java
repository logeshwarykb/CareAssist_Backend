package com.springboot.medical_billing.dto.response;

import java.util.List;

public record InsurancePlanFilterDto(
        List<InsurancePlanResponseDto> list,
        PaginationDto paginationInfo
) {
}
