package com.sahnesen.api.sahnesen.service;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private BadgeService badgeService;

    @Test
    @DisplayName("Kullanıcı 500 takipçiye ulaştığında VISIONARY rozeti almalı")
    void shouldAssignLighthouseBadgeWhenScoreIsReached() {
        // GIVEN
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUsername("kutukalan");
        // UserMetrics ve badges seti başlangıçta boş
        mockUser.setMetrics(new UserMetrics());

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // WHEN
        // 500 puan (takipçi) gönderiyoruz
        badgeService.checkAndAssignBadges(userId, BadgeCategory.FOLLOWER, 500);

        // THEN
        assertTrue(mockUser.getMetrics().getBadges().contains(BadgeType.VISIONARY),
                "Kullanıcı 500 takipçiye ulaştığında VISIONARY rozeti almalı");
        verify(userRepository, times(1)).save(mockUser);

    }

}
