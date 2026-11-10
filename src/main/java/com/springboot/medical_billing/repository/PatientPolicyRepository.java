package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.dto.response.PatientPolicyResponseDto;
import com.springboot.medical_billing.model.PatientPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientPolicyRepository
        extends JpaRepository<PatientPolicy, Integer> {

    @Query("""
    SELECT pp.id AS id,
           p.id AS patientId,
           p.fullName AS patientName,
           ip.id AS planId,
           ip.name AS planName,
           pp.startDate AS startDate,
           pp.endDate AS endDate,
           pp.status AS status,
           e.id AS executiveId,
           e.name AS companyName
    FROM PatientPolicy pp
    JOIN pp.patient p
    JOIN pp.plan ip
    LEFT JOIN ip.executive e
    """)
    List<PatientPolicyResponseDto> getAllPatientPolicies(Pageable pageable);

    @Query("""
    SELECT pp.id AS id,
           p.id AS patientId,
           p.fullName AS patientName,
           ip.id AS planId,
           ip.name AS planName,
           pp.startDate AS startDate,
           pp.endDate AS endDate,
           pp.status AS status,
           e.id AS executiveId,
           e.name AS companyName
    FROM PatientPolicy pp
    JOIN pp.patient p
    JOIN pp.plan ip
    LEFT JOIN ip.executive e
    WHERE p.id = ?1
    """)
    List<PatientPolicyResponseDto> getPoliciesByPatientId(Integer patientId, Pageable pageable);

    @Query("select count(pp) > 0 from PatientPolicy pp where pp.plan.id = ?1")
    boolean isPlanSelected(Integer planId);
}
