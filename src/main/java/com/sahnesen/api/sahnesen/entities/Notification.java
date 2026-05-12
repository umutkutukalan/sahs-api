package com.sahnesen.api.sahnesen.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.sahnesen.api.sahnesen.enums.NotificationType;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean isRead = false; // Okundu bilgisi

    private String targetUrl; // Tıklayınca gideceği sayfa

    @CreationTimestamp
    private LocalDateTime createdAt;
}
