package com.sahnesen.api.sahnesen.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.enums.BadgeCategory;
import com.sahnesen.api.sahnesen.services.BadgeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DevTestController {
    private final BadgeService badgeService;

    @GetMapping("/api/dev/trigger-vizyoner")
    public String triggerBadge() {
        // ID'si 3 olan kullanıcıya, 500 puanlık FOLLOWER kategorisi yolluyorum
        badgeService.checkAndAssignBadges(3L, BadgeCategory.FOLLOWER, 500);
        return "Vizyoner rozeti tetiklendi! Tarayıcı konsolunu kontrol et.";
    }
}
