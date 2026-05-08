package com.sahnesen.api.sahnesen.services;

import java.util.ArrayList;
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
import com.sahnesen.api.sahnesen.entities.model.UserMetrics;
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
        UserMetrics metrics = user.getMetrics();

        Set<BadgeType> currentBadges = metrics.getBadges();
        boolean isUpdated = false;

        // Verilen kategori ve puana göre kazanılabilecek rozetleri kontrol et
        List<BadgeType> potentialBadges = new ArrayList<>(Arrays.stream(BadgeType.values())
                .filter(badge -> badge.getCategory() == category)
                .filter(badge -> score >= badge.getRequiredScore())
                .filter(badge -> !currentBadges.contains(badge))
                .toList());

        List<BadgeType> specialBadges = checkSpecialBadges(metrics, currentBadges);
        potentialBadges.addAll(specialBadges);

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
            userRepository.save(user);
        }

    }

    // Hibrit kontrol gerektiren özel rozetler için ayrı bir kontrol mekanizması
    private List<BadgeType> checkSpecialBadges(UserMetrics metrics, Set<BadgeType> currentBadges) {
        List<BadgeType> earned = new ArrayList<>();

        // ANA SAHNE OYUNCUSU: 50 içerik + 5 biletli gösteri
        if (!currentBadges.contains(BadgeType.MAIN_STAGE) &&
                metrics.getContentCount() >= 50 &&
                metrics.getTicketedShowCount() >= 5) {
            earned.add(BadgeType.MAIN_STAGE);
        }

        // Yarın öbür gün başka SPECIAL rozetler gelirse buraya if blokları olarak
        // ekleyeceğim
        return earned;
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
