package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.response.NotificationResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.model.Notification;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService; // The REAL service being tested
    @Mock
    private NotificationRepository notificationRepository;

    // a notification is saved for patient "john"
    @Test
    public void addTest() {
        User user = new User("john", "encodedPassword", Role.PATIENT);

        notificationService.add(user, "Claim 1 approved");

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verifyNoMoreInteractions(notificationRepository);
    }

    // patient "john" has 2 notifications
    @Test
    public void getMyNotificationsTest() {
        Notification n1 = new Notification(1, null, "Claim 1 approved", LocalDateTime.now());
        Notification n2 = new Notification(2, null, "Claim 2 rejected", LocalDateTime.now());

        when(notificationRepository.getByUsername("john")).thenReturn(List.of(n2, n1));

        List<NotificationResponseDto> result = notificationService.getMyNotifications("john");

        assertEquals(2, result.size());
        assertEquals("Claim 2 rejected", result.get(0).message());

        verify(notificationRepository, times(1)).getByUsername("john");
        verifyNoMoreInteractions(notificationRepository);
    }
}
