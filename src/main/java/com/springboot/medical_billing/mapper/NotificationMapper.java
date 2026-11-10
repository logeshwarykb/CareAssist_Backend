package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.response.NotificationResponseDto;
import com.springboot.medical_billing.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public static NotificationResponseDto convertEntityToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
