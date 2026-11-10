package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.ClaimPaymentDto;
import com.springboot.medical_billing.dto.response.ClaimPaymentResponseDto;
import com.springboot.medical_billing.model.ClaimPayment;
import org.springframework.stereotype.Component;

@Component
public class ClaimPaymentMapper {

    public ClaimPayment convertDtoToEntity(ClaimPaymentDto dto) {

        ClaimPayment claimPayment = new ClaimPayment();

        claimPayment.setPaymentDate(dto.paymentDate());
        claimPayment.setAmount(dto.amount());
        claimPayment.setPaymentMode(dto.paymentMode());
        claimPayment.setPaymentStatus(dto.paymentStatus());

        return claimPayment;
    }

    public ClaimPaymentResponseDto convertEntityToDto(ClaimPayment claimPayment) {

        return new ClaimPaymentResponseDto(
                claimPayment.getId(),
                claimPayment.getClaim().getId(),
                claimPayment.getPaymentDate(),
                claimPayment.getAmount(),
                claimPayment.getPaymentMode(),
                claimPayment.getPaymentStatus()
        );
    }
}