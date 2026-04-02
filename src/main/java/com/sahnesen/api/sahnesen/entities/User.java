package com.sahnesen.api.sahnesen.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sahnesen.api.sahnesen.entities.model.UserMetrics;
import com.sahnesen.api.sahnesen.entities.model.UserPreferences;
import com.sahnesen.api.sahnesen.entities.model.UserProfessional;
import com.sahnesen.api.sahnesen.enums.AccountStatus;
import com.sahnesen.api.sahnesen.enums.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Temel Kimlik Bilgileri
    @Column(unique = true, nullable = false)
    private String slug;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String surname;
    private String bio;
    private String motto;

    // Görsel Alanlar
    @Column(columnDefinition = "TEXT")
    private String profileImg;
    @Column(columnDefinition = "TEXT")
    private String coverImg;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Embedded
    @Builder.Default
    private UserMetrics metrics = new UserMetrics();

    @Embedded
    @Builder.Default
    private UserProfessional professional = new UserProfessional();

    @Embedded
    @Builder.Default
    private UserPreferences preferences = new UserPreferences();

    // Durum ve Konum Bilgileri
    private String city;
    private String district;

    @Builder.Default
    private boolean isVerified = false;
    private LocalDateTime lastLogin;

    // İlişkiler (Önceki yapından devam edebilirsin)
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<SocialMediaPlatform> socialPlatforms = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
