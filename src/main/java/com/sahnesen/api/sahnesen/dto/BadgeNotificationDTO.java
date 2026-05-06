package com.sahnesen.api.sahnesen.dto;

public record BadgeNotificationDTO(Long userId,
        String badgeName,
        String displayName,
        Long timestamp) {
}
