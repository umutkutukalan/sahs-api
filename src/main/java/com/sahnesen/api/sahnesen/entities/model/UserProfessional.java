package com.sahnesen.api.sahnesen.entities.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfessional {
    private String job;
    @Builder.Default private boolean isLookingForWork = false;
    private String availableFor; // Freelance, Tam Zamanlı vb.
    private String portfolioUrl;
    @Column(length = 1000)
    private String skills; // "React, Spring Boot, Figma" gibi
}
