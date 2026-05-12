package com.sahnesen.api.sahnesen.services;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sahnesen.api.sahnesen.dto.NotificationDTO;
import com.sahnesen.api.sahnesen.entities.Notification;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.NotificationType;
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
    public void createNotification(Long userId, String title, String message, NotificationType type,
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

        Notification savedNotification = notificationRepository.save(notification);

        NotificationDTO dto = NotificationDTO.builder()
                .id(savedNotification.getId())
                .title(savedNotification.getTitle())
                .message(savedNotification.getMessage())
                .type(savedNotification.getType())
                .targetUrl(savedNotification.getTargetUrl())
                .createdAt(savedNotification.getCreatedAt())
                .build();

        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, dto);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı."));

        if (!notification.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu bildirimi okumaya yetkiniz yok.");
        }

        notification.setRead(true);
        // @Transactional sayesinde save dememize bile gerek yok, otomatik güncellenir.
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(n -> n.setRead(true));
    }

}
