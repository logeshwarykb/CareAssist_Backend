package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Integer> {

    @Query("select a from Administrator a where a.user.username = ?1")
    Optional<Administrator> getByUsername(String username);
}