package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.ClaimPaymentDto;
import com.springboot.medical_billing.dto.response.ClaimPaymentResponseDto;
import com.springboot.medical_billing.service.ClaimPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimPaymentController {

    private final ClaimPaymentService claimPaymentService;

    @PostMapping("/api/claim-payment/add")
    public ClaimPaymentResponseDto addClaimPayment(@Valid @RequestBody ClaimPaymentDto claimPaymentDto) {
        return claimPaymentService.addClaimPayment(claimPaymentDto);
    }

    @GetMapping("/api/claim-payment/{id}")
    public ClaimPaymentResponseDto getClaimPaymentById(@PathVariable Integer id) {
        return claimPaymentService.getClaimPaymentById(id);
    }

    @GetMapping("/api/claim-payment/all")
    public List<ClaimPaymentResponseDto> getAllClaimPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimPaymentService.getAllClaimPayments(page, size);
    }

    @GetMapping("/api/claim-payment/claim/{claimId}")
    public List<ClaimPaymentResponseDto> getClaimPaymentsByClaimId(
            @PathVariable Integer claimId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return claimPaymentService.getClaimPaymentsByClaimId(claimId, page, size);
    }
}