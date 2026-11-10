package com.springboot.medical_billing.repository;
import com.springboot.medical_billing.dto.response.PatientResponseDto;
import com.springboot.medical_billing.model.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository <Patient, Integer> {
    @Query("""
        SELECT p.id AS id,
               u.id AS userId,
               u.username AS username,
               p.fullName AS fullName,
               p.dateOfBirth AS dateOfBirth,
               p.gender AS gender,
               p.contactNumber AS contactNumber,
               p.medicalHistory AS medicalHistory,
               p.invoiceFile AS invoiceFile,
               p.medicalFile AS medicalFile
        FROM Patient p
        JOIN p.user u
        """)
    List<PatientResponseDto> getAllPatient(Pageable pageable);

    @Query("select p from Patient p where p.user.username = ?1")
    Optional<Patient> getByUsername(String username);
}