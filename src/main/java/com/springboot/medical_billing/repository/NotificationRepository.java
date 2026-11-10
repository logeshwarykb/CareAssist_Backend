package com.springboot.medical_billing.repository;

import com.springboot.medical_billing.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // notifications of the logged-in user (latest first)
    @Query("select n from Notification n where n.user.username = ?1 order by n.id desc")
    List<Notification> getByUsername(String username);
}
