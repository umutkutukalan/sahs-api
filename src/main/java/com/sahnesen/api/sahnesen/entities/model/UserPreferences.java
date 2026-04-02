package com.sahnesen.api.sahnesen.entities.model;

import com.sahnesen.api.sahnesen.enums.ThemeType;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ThemeType theme = ThemeType.SYSTEM;
    @Builder.Default
    private boolean isPrivate = false;
    @Builder.Default
    private boolean allowDirectMessages = true;
    @Builder.Default
    private String language = "tr";
}