package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Yazarın kendi yazılarını listelemesi için
    Page<Post> findAllByUser_Username(String username, Pageable pageable);

    // Okuyucunun bir yazıyı bulması için (Sadece yayınlanmış olanlar)
    Optional<Post> findBySlugAndUser_UsernameAndIsPublishedTrue(String slug, String username);

    // Slug çakışması var mı kontrolü
    boolean existsBySlug(String slug);
}
