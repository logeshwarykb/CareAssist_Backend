package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.ClaimPaymentDto;
import com.springboot.medical_billing.dto.response.ClaimPaymentResponseDto;
import com.springboot.medical_billing.enums.*;
import com.springboot.medical_billing.mapper.ClaimPaymentMapper;
import com.springboot.medical_billing.model.*;
import com.springboot.medical_billing.repository.ClaimPaymentRepository;
import com.springboot.medical_billing.repository.ClaimRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class ClaimPaymentServiceTest {

    @InjectMocks
    private ClaimPaymentService claimPaymentService; // The REAL service being tested
    @Mock
    private ClaimPaymentRepository claimPaymentRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimPaymentMapper claimPaymentMapper;
    @Mock
    private NotificationService notificationService;


    private ClaimPaymentResponseDto getPaymentDto(Integer id) {
        return new ClaimPaymentResponseDto(id, 1, LocalDate.now(), 5000.0,
                PaymentMode.UPI, PaymentStatus.COMPLETED);
    }

    // claim 1 exists, so the payment is linked to the claim and saved
    @Test
    public void addClaimPaymentTest() {
        ClaimPaymentDto paymentDto = new ClaimPaymentDto(1, LocalDate.now(), 5000.0,
                PaymentMode.UPI, PaymentStatus.COMPLETED);
        // reimbursement claim of patient "john", approved by Star Health for 5000
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUser(new User("john", "encodedPassword", Role.PATIENT));
        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);
        executive.setName("Star Health");

        Claim claim = new Claim();
        claim.setId(1);
        claim.setPatient(patient);
        claim.setExecutive(executive);
        claim.setClaimType(ClaimType.REIMBURSEMENT);
        claim.setApprovedAmount(5000.0);
        claim.setStatus(ClaimStatus.APPROVED);
        ClaimPayment claimPayment = new ClaimPayment();

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));
        when(claimPaymentMapper.convertDtoToEntity(paymentDto)).thenReturn(claimPayment);
        when(claimPaymentRepository.save(claimPayment)).thenReturn(claimPayment);
        when(claimPaymentMapper.convertEntityToDto(claimPayment)).thenReturn(getPaymentDto(1));

        ClaimPaymentResponseDto result = claimPaymentService.addClaimPayment(paymentDto);

        // claim attached to payment
        assertEquals(claim, claimPayment.getClaim());
        assertEquals(PaymentStatus.COMPLETED, result.paymentStatus());

        // after payment the claim becomes PAID
        assertEquals(ClaimStatus.PAID, claim.getStatus());

        verify(claimRepository, times(1)).findById(1);
        verify(claimPaymentRepository, times(1)).save(claimPayment);
        verify(claimRepository, times(1)).save(claim);
        // patient gets a notification
        verify(notificationService, times(1)).add(eq(patient.getUser()), anyString());
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(claimPaymentRepository);
    }

    // claim 7 doesn't exist (invalid id), so payment is not saved
    @Test
    public void addClaimPaymentTest_Invalid() {
        ClaimPaymentDto paymentDto = new ClaimPaymentDto(7, LocalDate.now(), 5000.0,
                PaymentMode.UPI, PaymentStatus.COMPLETED);

        when(claimRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> claimPaymentService.addClaimPayment(paymentDto));

        verify(claimRepository, times(1)).findById(7);
        // save was NEVER called
        verify(claimPaymentRepository, times(0)).save(any(ClaimPayment.class));
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(claimPaymentRepository);
        verifyNoMoreInteractions(claimPaymentMapper);
    }

    // payment id 1 exists and the result has the correct amount
    @Test
    public void getClaimPaymentByIdTest() {
        ClaimPayment claimPayment = new ClaimPayment();
        claimPayment.setId(1);

        when(claimPaymentRepository.findById(1)).thenReturn(Optional.of(claimPayment));
        when(claimPaymentMapper.convertEntityToDto(claimPayment)).thenReturn(getPaymentDto(1));

        assertEquals(5000.0, claimPaymentService.getClaimPaymentById(1).amount());

        verify(claimPaymentRepository, times(1)).findById(1);
        verifyNoMoreInteractions(claimPaymentRepository);
    }

    // payment id 7 doesn't exist (invalid id)
    @Test
    public void getClaimPaymentByIdTest_Invalid() {
        when(claimPaymentRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> claimPaymentService.getClaimPaymentById(7));

        verify(claimPaymentRepository, times(1)).findById(7);
        verifyNoMoreInteractions(claimPaymentRepository);
        verifyNoMoreInteractions(claimPaymentMapper);
    }

    // fetching all payments: 2 payments in DB so 2 results come back
    @Test
    public void getAllClaimPaymentsTest() {
        ClaimPayment payment1 = new ClaimPayment();
        ClaimPayment payment2 = new ClaimPayment();

        when(claimPaymentRepository.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(payment1, payment2)));
        when(claimPaymentMapper.convertEntityToDto(payment1)).thenReturn(getPaymentDto(1));
        when(claimPaymentMapper.convertEntityToDto(payment2)).thenReturn(getPaymentDto(2));

        assertEquals(2, claimPaymentService.getAllClaimPayments(0, 5).size());

        verify(claimPaymentRepository, times(1)).findAll(PageRequest.of(0, 5));
        verifyNoMoreInteractions(claimPaymentRepository);
    }

    // claim 1 exists, so its payments are returned
    @Test
    public void getClaimPaymentsByClaimIdTest() {
        ClaimPayment payment1 = new ClaimPayment();

        when(claimRepository.existsById(1)).thenReturn(true);
        when(claimPaymentRepository.getClaimPaymentsByClaimId(1, PageRequest.of(0, 5)))
                .thenReturn(List.of(payment1));
        when(claimPaymentMapper.convertEntityToDto(payment1)).thenReturn(getPaymentDto(1));

        assertEquals(1, claimPaymentService.getClaimPaymentsByClaimId(1, 0, 5).size());

        verify(claimRepository, times(1)).existsById(1);
        verify(claimPaymentRepository, times(1)).getClaimPaymentsByClaimId(1, PageRequest.of(0, 5));
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(claimPaymentRepository);
    }

    //  claim 7 doesn't exist (invalid id), so payments are never fetched
    @Test
    public void getClaimPaymentsByClaimIdTest_Invalid() {
        when(claimRepository.existsById(7)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> claimPaymentService.getClaimPaymentsByClaimId(7, 0, 5));

        verify(claimRepository, times(1)).existsById(7);
        verify(claimPaymentRepository, times(0)).getClaimPaymentsByClaimId(7, PageRequest.of(0, 5));
        verifyNoMoreInteractions(claimRepository);
        verifyNoMoreInteractions(claimPaymentRepository);
    }

    // claim is only SUBMITTED (not approved), so payment is not allowed
    @Test
    public void addClaimPaymentTest_NotApproved() {
        ClaimPaymentDto paymentDto = new ClaimPaymentDto(1, LocalDate.now(), 5000.0,
                PaymentMode.UPI, PaymentStatus.COMPLETED);
        Claim claim = new Claim();
        claim.setId(1);
        claim.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimPaymentService.addClaimPayment(paymentDto));

        verify(claimRepository, times(1)).findById(1);
        verify(claimPaymentRepository, times(0)).save(any(ClaimPayment.class));
        verifyNoMoreInteractions(claimPaymentRepository);
        verifyNoMoreInteractions(claimPaymentMapper);
    }

    // TPA approved only 55000 for the claim, so insurance cannot pay 58000
    @Test
    public void addClaimPaymentTest_MoreThanApproved() {
        ClaimPaymentDto paymentDto = new ClaimPaymentDto(1, LocalDate.now(), 58000.0,
                PaymentMode.BANK_TRANSFER, PaymentStatus.COMPLETED);
        Claim claim = new Claim();
        claim.setId(1);
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(55000.0);

        when(claimRepository.findById(1)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class,
                () -> claimPaymentService.addClaimPayment(paymentDto));

        verify(claimRepository, times(1)).findById(1);
        verify(claimPaymentRepository, times(0)).save(any(ClaimPayment.class));
        verifyNoMoreInteractions(claimPaymentRepository);
        verifyNoMoreInteractions(claimPaymentMapper);
    }
}
