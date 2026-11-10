package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.response.NotificationResponseDto;
import com.springboot.medical_billing.mapper.NotificationMapper;
import com.springboot.medical_billing.model.Notification;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // save a notification for this user (called from other services)
    public void add(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // notifications of the logged-in user
    public List<NotificationResponseDto> getMyNotifications(String username) {
        return notificationRepository.getByUsername(username)
                .stream()
                .map(NotificationMapper::convertEntityToDto)
                .toList();
    }
}
