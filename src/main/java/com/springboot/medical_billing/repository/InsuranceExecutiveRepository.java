package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.InsuranceExecutive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InsuranceExecutiveRepository extends JpaRepository<InsuranceExecutive, Integer> {

    @Query("select e from InsuranceExecutive e where e.user.username = ?1")
    Optional<InsuranceExecutive> getByUsername(String username);
}