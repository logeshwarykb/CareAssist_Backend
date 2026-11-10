package com.springboot.medical_billing.dto.response;

public record PaginationDto(
        int totalPages,
        long totalRecords,
        int numRecordsInPage
) {
}
