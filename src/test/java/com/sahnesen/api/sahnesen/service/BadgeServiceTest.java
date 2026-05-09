package com.sahnesen.api.sahnesen.service;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.entities.model.UserMetrics;
import com.sahnesen.api.sahnesen.enums.BadgeCategory;
import com.sahnesen.api.sahnesen.enums.BadgeType;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.services.BadgeService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BadgeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    @DisplayName("Kullanıcı 100 takipçiye ulaştığında VERIFIED rozeti almalı")
    void shouldAssignVerifiedBadgeWhenScoreIsReached() {
        // GIVEN
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUsername("kutukalan");
        // UserMetrics ve badges seti başlangıçta boş
        UserMetrics metrics = new UserMetrics();
        metrics.setTicketedShowCount(5);
        metrics.setContentCount(0);
        mockUser.setMetrics(metrics);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // WHEN
        // 100 puan (takipçi) gönderiyoruz
        badgeService.checkAndAssignBadges(userId, BadgeCategory.FOLLOWER, 100);
        metrics.setContentCount(50); // 50 içerik şartını manuel setliyoruz
        badgeService.checkAndAssignBadges(userId, BadgeCategory.CONTENT_COUNT, 50);

        // THEN
        assertTrue(mockUser.getMetrics().getBadges().contains(BadgeType.VERIFIED),
                "Kullanıcı 100 takipçiye ulaştığında VERIFIED rozeti almalı");
        assertTrue(mockUser.getMetrics().getBadges().contains(BadgeType.MASTER_ACTOR),
                "Kullanıcı 50 içerik oluşturduğunda MASTER_ACTOR rozeti almalı");
        assertTrue(mockUser.getMetrics().getBadges().contains(BadgeType.MAIN_STAGE),
                "Kullanıcı 50 içerik ve 5 biletli gösteri düzenlediğinde MAIN_STAGE rozeti almalı");
        verify(userRepository, times(2)).save(mockUser);

    }
}