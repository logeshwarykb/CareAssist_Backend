package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.PreAuthorization;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreAuthorizationRepository extends JpaRepository<PreAuthorization, Integer> {

    @Query("SELECT pa FROM PreAuthorization pa WHERE pa.tpa.id = ?1")
    List<PreAuthorization> getPreAuthorizationsByTpaId(Integer tpaId, Pageable pageable);

    @Query("SELECT pa FROM PreAuthorization pa WHERE pa.healthcareProvider.id = ?1")
    List<PreAuthorization> getPreAuthorizationsByProviderId(Integer providerId, Pageable pageable);

    @Query("SELECT pa FROM PreAuthorization pa WHERE pa.patient.id = ?1")
    List<PreAuthorization> getPreAuthorizationsByPatientId(Integer patientId, Pageable pageable);

    @Query("SELECT pa FROM PreAuthorization pa WHERE pa.patient.user.username = ?1 ORDER BY pa.id DESC")
    List<PreAuthorization> getPreAuthorizationsByPatientUsername(String username, Pageable pageable);
}
