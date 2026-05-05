package com.sahnesen.api.sahnesen.enums;

import lombok.Getter;

@Getter
public enum BadgeType {
    // Takipçi Odaklı
    VISIONARY(500, "Vizyoner", BadgeCategory.FOLLOWER),
    
    // İçerik Odaklı
    CONTENT_CREATOR(5, "Üretken", BadgeCategory.CONTENT_COUNT),
    
    // Okunma/Etkileşim Odaklı
    BOOKWORM(1000, "Kütüphane Sakini", BadgeCategory.READING_TIME);

    private final int requiredScore;
    private final String displayName;
    private final BadgeCategory category;

    BadgeType(int requiredScore, String displayName, BadgeCategory category) {
        this.requiredScore = requiredScore;
        this.displayName = displayName;
        this.category = category;
    }
}
