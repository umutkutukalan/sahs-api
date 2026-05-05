package com.sahnesen.api.sahnesen.services;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.BadgeType;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final UserRepository userRepository;

    @Transactional
    public void checkAndAssignBadges(String username, Integer currentFollowerCount) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Set<BadgeType> currentBadges = user.getMetrics().getBadges();
        boolean isUpdated = false;

        for (BadgeType badge : BadgeType.values()) {
            // Eğer kullanıcının puanı/takipçisi yetiyorsa ve rozete henüz sahip değilse
            if (currentFollowerCount >= badge.getRequiredScore() && !currentBadges.contains(badge)) {
                currentBadges.add(badge);
                isUpdated = true;
                // Burada ileride Web Socket ile "Yeni rozet kazandın!" bildirimi atacağız
            }
        }

        if (isUpdated) {
            user.getMetrics().setBadges(currentBadges);
            userRepository.save(user);
        }

    }

}
