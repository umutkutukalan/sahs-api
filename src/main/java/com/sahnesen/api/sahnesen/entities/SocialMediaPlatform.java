package com.sahnesen.api.sahnesen.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sahnesen.api.sahnesen.enums.PlatformType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "user_social_platform", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "platform" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMediaPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PlatformType platform;

    @Column(length = 100)
    private String username;

    @Column(length = 500)
    private String url;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPublic = true;

    // Constructor - Temel
    public SocialMediaPlatform(User user, PlatformType platform) {
        this.user = user;
        this.platform = platform;
        this.isPublic = true;
    }

    // Constructor - Full (URL otomatik üretilir)
    public SocialMediaPlatform(User user, PlatformType platform, String username) {
        this(user, platform);
        this.username = username;
        this.url = platform.generateFullUrl(username);
    }

    /**
     * Eğer kullanıcı elle bir URL girmek isterse (özellikle WEBSITE tipi için)
     * veya username güncellendiğinde tetiklenebilir.
     */
    public void updateUrlByUsername() {
        if (this.platform != null && this.username != null) {
            this.url = this.platform.generateFullUrl(this.username);
        }
    }
}
