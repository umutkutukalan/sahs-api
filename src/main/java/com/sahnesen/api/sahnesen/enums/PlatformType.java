package com.sahnesen.api.sahnesen.enums;

import lombok.Getter;

@Getter
public enum PlatformType {
    GITHUB("https://github.com/"),
    LINKEDIN("https://linkedin.com/in/"),
    TWITTER("https://x.com/"),
    INSTAGRAM("https://instagram.com/"),
    YOUTUBE("https://youtube.com/@"),
    WEBSITE(""); // Özel web siteleri için boş prefix

    private final String urlPrefix;

    PlatformType(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String generateFullUrl(String username) {
        if (username == null || username.trim().isEmpty())
            return null;
        String cleanUsername = username.startsWith("@") ? username.substring(1) : username;
        return this.urlPrefix + cleanUsername;
    }
}