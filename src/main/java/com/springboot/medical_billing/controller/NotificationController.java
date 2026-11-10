package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.response.NotificationResponseDto;
import com.springboot.medical_billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    // notifications of the logged-in user
    @GetMapping("/api/notification/my")
    public List<NotificationResponseDto> getMyNotifications(Principal principal) {
        return notificationService.getMyNotifications(principal.getName());
    }
}
