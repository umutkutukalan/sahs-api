package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Belirli bir kullanıcının takip ettiklerini sayfalayarak listele
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

    // Belirli bir kullanıcının takipçilerini sayfalayarak listele
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);

    // Güvenlik ve mükerrer kayıt kontrolü için kritik metod
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Redis Cache Miss durumunda veya veri doğrulama için gerekli count'lar
    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    // İleride "Takipçilerim arasında ara" özelliği getirirsem diye:
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}