package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Belirli bir kullanıcının yayınlanmış yazılarını getir (Profil sayfası için)
    Page<Post> findAllByUser_UsernameAndIsPublishedTrueOrderByCreatedAtDesc(String username, Pageable pageable);

    // Tüm yayınlanmış yazıları getir (Ana sayfa akışı için)
    Page<Post> findAllByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    // Belirli bir kullanıcının tüm yazılarını getir (Yazarın kendi yönetim paneli için)
    Page<Post> findAllByUser_UsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    Optional<Post> findBySlug(String slug);

    Optional<Post> findBySlugAndIsPublishedTrue(String slug);

    // Slug çakışması var mı kontrolü
    boolean existsBySlug(String slug);
}
