package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.Tpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TpaRepository extends JpaRepository<Tpa, Integer> {

    @Query("select t from Tpa t where t.user.username = ?1")
    Optional<Tpa> getByUsername(String username);
}
