package com.springboot.medical_billing.dto.response;

import com.springboot.medical_billing.enums.PaymentMode;
import com.springboot.medical_billing.enums.PaymentStatus;

import java.time.LocalDate;

public record ClaimPaymentResponseDto(
        Integer id,
        Integer claimId,
        LocalDate paymentDate,
        double amount,
        PaymentMode paymentMode,
        PaymentStatus paymentStatus
) {
}