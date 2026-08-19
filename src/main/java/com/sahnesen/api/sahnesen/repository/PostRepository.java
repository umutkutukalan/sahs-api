package com.sahnesen.api.sahnesen.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. Yayınlanmış tüm yazılar (Ana Sayfa Akışı)
    Page<Post> findAllByIsPublishedTrue(Pageable pageable);

    // 2. Belirli bir kullanıcının yayınlanmış yazıları (Profil Sayfası)
    Page<Post> findAllByUser_UsernameAndIsPublishedTrue(String username, Pageable pageable);

    // 3. Kullanıcının yayınlanma durumuna göre yazıları (Taslak / Yayınlanan)
    Page<Post> findAllByUser_UsernameAndIsPublished(String username, boolean isPublished, Pageable pageable);

    // 4. Belirli bir kullanıcının tüm yazıları
    Page<Post> findAllByUser_Username(String username, Pageable pageable);

    Optional<Post> findBySlug(String slug);

    Optional<Post> findBySlugAndIsPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    List<Post> findAllByIsPublishedFalseAndUpdatedAtBefore(LocalDateTime dateTime);
}
