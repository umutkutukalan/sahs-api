package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.BadgeType;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findBySlug(String slug);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Sahnesen Özel: En yüksek puana sahip yazarları getir (Spotlight için)
    List<User> findTop10ByOrderByMetricsReputationScoreDesc();

    // Rozet Avcıları: Belirli bir rozete (BadgeType) sahip olanları getir
    @Query("SELECT u FROM User u WHERE :badge MEMBER OF u.metrics.badges")
    List<User> findAllByBadgeType(@Param("badge") BadgeType badge);

}
