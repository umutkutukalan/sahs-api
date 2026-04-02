package com.sahnesen.api.sahnesen.enums;

import lombok.Getter;

@Getter
public enum BadgeType {
    PIONEER(0, "Öncü Üye"),
    LIGHTHOUSE(50, "Yol Gösterici"),
    ARCHITECT(100, "Mimar"),
    VISIONARY(500, "Vizyoner");

    private final int requiredScore;
    private final String displayName;

    BadgeType(int requiredScore, String displayName) {
        this.requiredScore = requiredScore;
        this.displayName = displayName;
    }
}