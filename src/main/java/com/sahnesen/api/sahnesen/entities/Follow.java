package com.sahnesen.api.sahnesen.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sahnesen.api.sahnesen.enums.FollowStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "follows", uniqueConstraints = {
        // Aynı kişinin aynı kişiyi birden fazla kez takip etmesini engeller.
        @UniqueConstraint(columnNames = { "follower_id", "following_id" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Takip eden kişi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    @JsonIgnoreProperties({ "password", "following", "followers" })
    private User follower;

    // Takip edilen kişi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    @JsonIgnoreProperties({ "password", "following", "followers" })
    private User following;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FollowStatus status = FollowStatus.ACCEPTED; // Varsayılan olarak takip işlemi kabul edilmiş olarak başlar

    @Builder.Default
    private boolean isMuted = false; // Sessize alma durumu (ACCEPTED iken de olabilir)

    @Builder.Default
    private LocalDateTime followedAt = LocalDateTime.now();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt; // İstek kabul edildiğinde veya mute edildiğinde güncellenir
}
