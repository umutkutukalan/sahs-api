package com.sahnesen.api.sahnesen.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.BadgeNotificationDTO;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.BadgeCategory;
import com.sahnesen.api.sahnesen.enums.BadgeType;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket mesajlarını göndermek için kullanacağımız
                                                           // template

    @Transactional
    public void checkAndAssignBadges(Long userId, BadgeCategory category, int score) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        Set<BadgeType> currentBadges = user.getMetrics().getBadges();
        boolean isUpdated = false;

        // Verilen kategori ve puana göre kazanılabilecek rozetleri kontrol et
        List<BadgeType> potentialBadges = Arrays.stream(BadgeType.values())
                .filter(badge -> badge.getCategory() == category) // Kategori kontrolü
                .filter(badge -> score >= badge.getRequiredScore()) // Puan Kontrolü
                .filter(badge -> !currentBadges.contains(badge)) // Sahiplik kontrolü
                .toList();

        // Eğer yeni kazanılan rozetler varsa sete ekle
        if (!potentialBadges.isEmpty()) {
            currentBadges.addAll(potentialBadges);
            isUpdated = true;

            potentialBadges.forEach(badge -> {
                log.info("Kullanıcı {} yeni bir rozet kazandı: {}", user.getUsername(), badge.getDisplayName());
                sendBadgeNotification(userId, badge); // Bildirimi tetikleyen çağrı
            });
        }

        // Eğer rozetlerde bir güncelleme varsa DB'ye yansıt
        if (isUpdated) {
            user.getMetrics().setBadges(currentBadges);
            userRepository.save(user);
        }

    }

    private void sendBadgeNotification(Long userId, BadgeType badge) {
        // Hedef kanal: /topic/badges/1
        String destination = "/topic/badges/" + userId;

        // Gönderilecek veri (Payload)
        BadgeNotificationDTO notification = new BadgeNotificationDTO(userId, badge.name(), badge.getDisplayName(),
                System.currentTimeMillis());

        messagingTemplate.convertAndSend(destination, notification);
        log.info("WebSocket bildirimi gönderildi -> {}: {}", destination, badge.getDisplayName());
    }

}
