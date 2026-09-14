package com.sahnesen.api.sahnesen.services;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.sahnesen.api.sahnesen.dto.NotificationDTO;
import com.sahnesen.api.sahnesen.dto.UserNotificationDTO;
import com.sahnesen.api.sahnesen.entities.Notification;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.NotificationType;
import com.sahnesen.api.sahnesen.repository.FollowRepository;
import com.sahnesen.api.sahnesen.repository.NotificationRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket mesajlaşma için

    @Transactional
    public void createNotification(Long userId, Long senderId, String title, String message, NotificationType type,
            String targetUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Bildirim gönderilecek kullanıcı bulunamadı."));

        User sender = null;
        if (senderId != null) {
            sender = userRepository.findById(senderId).orElse(null);
        }

        Notification notification = Notification.builder()
                .user(user)
                .sender(sender)
                .title(title)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Sender varsa PublicUserDTO'ya dönüştür
        UserNotificationDTO senderDTO = null;
        if (savedNotification.getSender() != null) {
            User s = savedNotification.getSender();
            senderDTO = UserNotificationDTO.builder()
                    .id(s.getId())
                    .username(s.getUsername())
                    .name(s.getName())
                    .surname(s.getSurname())
                    .slug(s.getSlug())
                    .profileImg(s.getProfileImg())
                    .role(s.getRole() != null ? s.getRole().name() : null)
                    .build();
        }

        NotificationDTO dto = NotificationDTO.builder()
                .id(savedNotification.getId())
                .title(savedNotification.getTitle())
                .message(savedNotification.getMessage())
                .type(savedNotification.getType())
                .targetUrl(savedNotification.getTargetUrl())
                .isRead(savedNotification.isRead())
                .createdAt(savedNotification.getCreatedAt())
                .sender(senderDTO)
                .build();

        String destination = "/topic/notifications/" + userId;
        messagingTemplate.convertAndSend(destination, dto);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 1. Bu bildirimlerdeki tüm sender ID'lerini topla (null olanları ayıkla)
        Set<Long> senderIds = notifications.stream()
                .map(n -> n.getSender() != null ? n.getSender().getId() : null)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. Giriş yapan kullanıcının (userId) bu sender'lardan hangilerini takip
        // ettiğini tek sorguda çek
        Set<Long> followedSenderIds = senderIds.isEmpty() ? Collections.emptySet()
                : followRepository.findFollowingIdsByFollowerIdAndTargetIds(userId, senderIds);

        return notifications.stream().map(n -> {
            UserNotificationDTO senderDTO = null;
            if (n.getSender() != null) {
                User s = n.getSender();
                boolean isFollowing = followedSenderIds.contains(s.getId());

                senderDTO = UserNotificationDTO.builder()
                        .id(s.getId())
                        .username(s.getUsername())
                        .name(s.getName())
                        .surname(s.getSurname())
                        .slug(s.getSlug())
                        .profileImg(s.getProfileImg())
                        .role(s.getRole() != null ? s.getRole().name() : null)
                        .isFollowing(isFollowing)
                        .build();
            }

            return NotificationDTO.builder()
                    .id(n.getId())
                    .title(n.getTitle())
                    .message(n.getMessage())
                    .type(n.getType())
                    .targetUrl(n.getTargetUrl())
                    .isRead(n.isRead())
                    .createdAt(n.getCreatedAt())
                    .sender(senderDTO)
                    .build();
        }).toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Bildirim bulunamadı."));

        if (!notification.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu bildirimi okumaya yetkiniz yok.");
        }

        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);

        String destination = "/topic/notifications/" + userId;
        NotificationDTO resetDto = NotificationDTO.builder()
                .type(NotificationType.SYSTEM)
                .message("READ_ALL")
                .build();
        messagingTemplate.convertAndSend(destination, resetDto);
    }

    @Transactional
    public void notifyFollowers(Long authorId, String authorName, String postTitle, String postSlug) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Yazar bulunamadı."));

        String title = "Yeni Sahne!";
        String message = authorName + " yeni bir içerik paylaştı: " + postTitle;
        String targetUrl = "/post/" + postSlug;

        author.getFollowers().forEach(follower -> {
            // Takipçi bildirimlerinde gönderen (sender) olarak yazarın ID'sini geçiyoruz
            createNotification(follower.getId(), authorId, title, message, NotificationType.FOLLOWED_USER_POST,
                    targetUrl);
        });
    }
}