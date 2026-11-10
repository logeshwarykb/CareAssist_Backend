package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.ClaimPayment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClaimPaymentRepository extends JpaRepository<ClaimPayment, Integer> {

    @Query("SELECT cp FROM ClaimPayment cp WHERE cp.claim.id = ?1")
    List<ClaimPayment> getClaimPaymentsByClaimId(Integer claimId, Pageable pageable);
}