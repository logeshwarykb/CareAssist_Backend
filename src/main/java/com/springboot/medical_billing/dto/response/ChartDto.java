package com.springboot.medical_billing.dto.response;

import java.util.List;

public record ChartDto(
        List<String> labels,
        List<Long> values
) {
}