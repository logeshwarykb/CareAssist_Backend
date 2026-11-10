package com.springboot.medical_billing.dto.response;

import java.util.List;

public record ClaimFilterDto(
        List<ClaimResponseDto> list,
        PaginationDto paginationInfo
) {
}