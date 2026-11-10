package com.springboot.medical_billing.dto.request;

import com.springboot.medical_billing.enums.PaymentMode;
import com.springboot.medical_billing.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ClaimPaymentDto(

        @NotNull(message = "Claim ID is required")
        Integer claimId,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @DecimalMin(value = "0.0", inclusive = false,
                message = "Payment amount must be greater than zero")
        double amount,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode,

        @NotNull(message = "Payment status is required")
        PaymentStatus paymentStatus

) {
}