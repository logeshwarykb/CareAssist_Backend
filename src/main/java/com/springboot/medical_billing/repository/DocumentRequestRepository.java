package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.DocumentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Integer> {

    @Query("select d from DocumentRequest d where d.patient.user.username = ?1 order by d.id desc")
    List<DocumentRequest> getByPatientUsername(String username);

    @Query("select d from DocumentRequest d where d.healthcareProvider.user.username = ?1 order by d.id desc")
    List<DocumentRequest> getByProviderUsername(String username);
}
