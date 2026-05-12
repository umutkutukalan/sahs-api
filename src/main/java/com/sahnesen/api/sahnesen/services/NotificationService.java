package com.sahnesen.api.sahnesen.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sahnesen.api.sahnesen.entities.Notification;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.NotificationsType;
import com.sahnesen.api.sahnesen.repository.NotificationRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket mesajlaşma için

    @Transactional
    public void createNotification(Long userId, String title, String message, NotificationsType type,
            String targetUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bildirim gönderilecek kullanıcı bulunamadı."));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, notification);

    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> n.setRead(true));
    }

}
