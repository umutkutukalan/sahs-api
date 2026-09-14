package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sahnesen.api.sahnesen.entities.Follow;

import io.lettuce.core.dynamic.annotation.Param;

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

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.following.id IN :senderIds")
    Set<Long> findFollowingIdsByFollowerIdAndTargetIds(@Param("followerId") Long followerId,
            @Param("senderIds") Set<Long> senderIds);
}