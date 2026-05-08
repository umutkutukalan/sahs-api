package com.sahnesen.api.sahnesen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.entities.model.UserMetrics;
import com.sahnesen.api.sahnesen.enums.BadgeCategory;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.services.BadgeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DevTestController {
    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping("/api/dev/trigger-main-stage")
    public String triggerMainStage() {
        // ID'si 3 olan kullanıcıyı bulup metriklerini güncelleyelim
        User user = userRepository.findById(3L).orElseThrow();
        UserMetrics metrics = user.getMetrics();
        
        // Hibrit şartları manuel setliyoruz
        metrics.setContentCount(50);
        metrics.setTicketedShowCount(5);
        
        userRepository.save(user);

        // Şimdi motoru tetikliyoruz. Kategori önemli değil çünkü 
        // metot artık her çağrıldığında SPECIAL rozetleri de check ediyor.
        badgeService.checkAndAssignBadges(3L, BadgeCategory.CONTENT_COUNT, 50);
        
        return "Hibrit rozet (Ana Sahne Oyuncusu) için şartlar sağlandı ve tetiklendi!";
    }
}
