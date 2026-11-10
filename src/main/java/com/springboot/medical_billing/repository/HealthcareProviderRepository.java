package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.HealthcareProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthcareProviderRepository extends JpaRepository<HealthcareProvider, Integer> {

    @Query("select hp from HealthcareProvider hp where hp.user.username = ?1")
    Optional<HealthcareProvider> getByUsername(String username);
}