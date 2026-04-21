package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sahnesen.api.sahnesen.entities.Follow;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Belirli bir kullanıcının takip ettiklerini listele (Following list)
    List<Follow> findByFollowerId(Long followerId);

    // Belirli bir kullanıcının takipçilerini listele (Followers list)
    List<Follow> findByFollowingId(Long followingId);

    // Güvenlik ve mükerrer kayıt kontrolü için kritik metod
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Redis Cache Miss durumunda veya veri doğrulama için gerekli count'lar
    long countByFollowerId(Long followerId);

    long countByFollowingId(Long followingId);

    // İleride "Takipçilerim arasında ara" özelliği getirirsem diye:
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

}
