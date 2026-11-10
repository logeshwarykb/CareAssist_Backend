package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.dto.response.ClaimResponseDto;
import com.springboot.medical_billing.dto.response.ClaimStatusCountDto;
import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import com.springboot.medical_billing.model.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Integer> {
    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        """)
    List<ClaimResponseDto> getAllClaims(Pageable pageable);

    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        WHERE p.id = ?1
        """)
    List<ClaimResponseDto> getClaimsByPatientId(
            Integer patientId, Pageable pageable);

    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        WHERE p.user.username = ?1
        """)
    List<ClaimResponseDto> getClaimsByPatientUsername(String username, Pageable pageable);

    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        WHERE e.id = ?1
        """)
    List<ClaimResponseDto> getClaimsByExecutiveId(
            Integer executiveId, Pageable pageable);

    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        WHERE t.id = ?1
        """)
    List<ClaimResponseDto> getClaimsByTpaId(
            Integer tpaId, Pageable pageable);

    @Query("""
        SELECT c.id AS id,
               p.id AS patientId,
               p.fullName AS patientName,
               pp.id AS policyId,
               e.id AS executiveId,
               e.name AS executiveName,
               c.claimDate AS claimDate,
               c.claimAmount AS claimAmount,
               c.status AS status,
               c.claimType AS claimType,
               t.id AS tpaId,
               t.name AS tpaName,
               pa.id AS preAuthorizationId,
               c.approvedAmount AS approvedAmount,
               ph.name AS providerName,
               pe.id AS policyCompanyId,
               pe.name AS policyCompanyName,
               c.invoiceFile AS invoiceFile,
               c.medicalFile AS medicalFile,
               c.approvedDate AS approvedDate
        FROM Claim c
        JOIN c.patient p
        JOIN c.policy pp
        LEFT JOIN pp.plan pl
        LEFT JOIN pl.executive pe
        LEFT JOIN c.executive e
        LEFT JOIN c.tpa t
        LEFT JOIN c.preAuthorization pa
        LEFT JOIN pa.healthcareProvider ph
        WHERE ph.id = ?1
        """)
    List<ClaimResponseDto> getClaimsByProviderId(
            Integer providerId, Pageable pageable);

    @Query(value = """
    SELECT c.id AS id,
           p.id AS patientId,
           p.fullName AS patientName,
           pp.id AS policyId,
           e.id AS executiveId,
           e.name AS executiveName,
           c.claimDate AS claimDate,
           c.claimAmount AS claimAmount,
           c.status AS status,
           c.claimType AS claimType,
           t.id AS tpaId,
           t.name AS tpaName,
           pa.id AS preAuthorizationId,
           c.approvedAmount AS approvedAmount,
           ph.name AS providerName,
           pe.id AS policyCompanyId,
           pe.name AS policyCompanyName,
           c.invoiceFile AS invoiceFile,
           c.medicalFile AS medicalFile,
           c.approvedDate AS approvedDate
    FROM Claim c
    JOIN c.patient p
    JOIN c.policy pp
    LEFT JOIN pp.plan pl
    LEFT JOIN pl.executive pe
    LEFT JOIN c.executive e
    LEFT JOIN c.tpa t
    LEFT JOIN c.preAuthorization pa
    LEFT JOIN pa.healthcareProvider ph
    WHERE (?1 IS NULL OR p.id = ?1)
    AND (?2 IS NULL OR pp.id = ?2)
    AND (?3 IS NULL OR e.id = ?3)
    AND (?4 IS NULL OR c.status = ?4)
    AND (?5 IS NULL OR c.claimDate >= ?5)
    AND (?6 IS NULL OR c.claimDate <= ?6)
    AND (?7 IS NULL OR c.claimAmount >= ?7)
    AND (?8 IS NULL OR c.claimAmount <= ?8)
    AND (?9 IS NULL OR c.claimType = ?9)
    """,
            countQuery = """
    SELECT COUNT(c)
    FROM Claim c
    JOIN c.patient p
    JOIN c.policy pp
    LEFT JOIN c.executive e
    WHERE (?1 IS NULL OR p.id = ?1)
    AND (?2 IS NULL OR pp.id = ?2)
    AND (?3 IS NULL OR e.id = ?3)
    AND (?4 IS NULL OR c.status = ?4)
    AND (?5 IS NULL OR c.claimDate >= ?5)
    AND (?6 IS NULL OR c.claimDate <= ?6)
    AND (?7 IS NULL OR c.claimAmount >= ?7)
    AND (?8 IS NULL OR c.claimAmount <= ?8)
    AND (?9 IS NULL OR c.claimType = ?9)
    """)
    Page<ClaimResponseDto> filterClaims(
            Integer patientId,
            Integer policyId,
            Integer executiveId,
            ClaimStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Double minAmount,
            Double maxAmount,
            ClaimType claimType,
            Pageable pageable);

    @Query("select c.status as status, count(c) as count from Claim c group by c.status")
    List<ClaimStatusCountDto> getClaimCountByStatus();

    @Query("select c from Claim c where c.preAuthorization.id = ?1")
    Optional<Claim> getByPreAuthorizationId(Integer preAuthorizationId);
}