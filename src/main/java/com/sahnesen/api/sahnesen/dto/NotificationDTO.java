package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;

import com.sahnesen.api.sahnesen.enums.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private String targetUrl;
    private LocalDateTime createdAt;
}
