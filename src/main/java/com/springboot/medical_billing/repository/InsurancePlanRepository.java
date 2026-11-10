package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.dto.response.InsurancePlanResponseDto;
import com.springboot.medical_billing.model.InsurancePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Integer> {


    Optional<InsurancePlan> findByName(String name);

    @Query("""
            SELECT ip.id AS id,
                   ip.name AS name,
                   ip.coverageAmount AS coverageAmount,
                   ip.premiumAmount AS premiumAmount,
                   ip.coverageDetails AS coverageDetails,
                   ip.termsConditions AS termsConditions,
                   e.id AS executiveId,
                   e.name AS executiveName
            FROM InsurancePlan ip
            LEFT JOIN ip.executive e
            """)
    List<InsurancePlanResponseDto> getAllInsurancePlans(Pageable pageable);

    @Query(value = """
            SELECT ip.id AS id,
                   ip.name AS name,
                   ip.coverageAmount AS coverageAmount,
                   ip.premiumAmount AS premiumAmount,
                   ip.coverageDetails AS coverageDetails,
                   ip.termsConditions AS termsConditions,
                   e.id AS executiveId,
                   e.name AS executiveName
            FROM InsurancePlan ip
            LEFT JOIN ip.executive e
            WHERE (?1 IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', ?1, '%')))
            AND (?2 IS NULL OR ip.coverageAmount >= ?2)
            AND (?3 IS NULL OR ip.coverageAmount <= ?3)
            AND (?4 IS NULL OR ip.premiumAmount >= ?4)
            AND (?5 IS NULL OR ip.premiumAmount <= ?5)
            """,
            countQuery = """
            SELECT COUNT(ip)
            FROM InsurancePlan ip
            WHERE (?1 IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', ?1, '%')))
            AND (?2 IS NULL OR ip.coverageAmount >= ?2)
            AND (?3 IS NULL OR ip.coverageAmount <= ?3)
            AND (?4 IS NULL OR ip.premiumAmount >= ?4)
            AND (?5 IS NULL OR ip.premiumAmount <= ?5)
            """)
    Page<InsurancePlanResponseDto> filterInsurancePlans(
            String name,
            Double minCoverage,
            Double maxCoverage,
            Double minPremium,
            Double maxPremium,
            Pageable pageable);

    @Query(value = """
            SELECT ip FROM InsurancePlan ip
            LEFT JOIN ip.executive e
            LEFT JOIN e.user u
            WHERE (?1 IS NULL OR u.username = ?1)
            AND (?2 IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', ?2, '%')))
            """,
            countQuery = """
            SELECT COUNT(ip) FROM InsurancePlan ip
            LEFT JOIN ip.executive e
            LEFT JOIN e.user u
            WHERE (?1 IS NULL OR u.username = ?1)
            AND (?2 IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', ?2, '%')))
            """)
    Page<InsurancePlan> searchPlans(String executiveUsername, String name, Pageable pageable);
}
