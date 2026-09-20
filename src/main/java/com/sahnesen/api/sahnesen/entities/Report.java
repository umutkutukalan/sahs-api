package com.sahnesen.api.sahnesen.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.sahnesen.api.sahnesen.enums.ReportType;

import jakarta.persistence.Column;
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
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter; // Raporlayan kullanıcı

    @Column(nullable = false)
    private Long targetId; // Raporlanan içerik ID'si (Örn: Post ID)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType; // İçerik tipi (POST, COMMENT, USER vb.)

    private String reason; // Rapor sebebi (Spam, taciz vs.)

    @CreationTimestamp
    private LocalDateTime createdAt;
}
