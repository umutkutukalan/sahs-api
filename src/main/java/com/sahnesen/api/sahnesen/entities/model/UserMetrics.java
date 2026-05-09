package com.sahnesen.api.sahnesen.entities.model;

import java.util.HashSet;
import java.util.Set;

import com.sahnesen.api.sahnesen.enums.BadgeType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMetrics {
    @Builder.Default
    private Integer contentCount = 0;
    @Builder.Default
    private Long totalReadingTime = 0L;
    @Builder.Default
    private Integer followerCount = 0;

    @Builder.Default
    private Integer ticketedShowCount = 0;
    @Builder.Default
    private Integer totalLightCount = 0;
    @Builder.Default
    private Integer totalSignatureCount = 0;

    @Builder.Default
    private Integer profileViews = 0; // Yeni: Profil kaç kez görüntülendi?
    @Builder.Default
    private Double reputationScore = 0.0; // Yeni: Sahnesen puanı

    @ElementCollection(targetClass = BadgeType.class)
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING) // Veritabanında String olarak sakla (örn: "ARCHITECT")
    @Column(name = "badges", length = 100) // Rozet isimleri uzun olabilir, bu yüzden length'i artırıyoruz
    @Builder.Default
    private Set<BadgeType> badges = new HashSet<>();
}
