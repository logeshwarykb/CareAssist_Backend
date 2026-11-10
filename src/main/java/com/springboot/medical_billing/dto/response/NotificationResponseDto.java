package com.springboot.medical_billing.dto.response;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Integer id,
        String message,
        LocalDateTime createdAt
) {
}
