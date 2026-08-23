package com.sahnesen.api.sahnesen.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.enums.PostType;

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

        // Mevcut id hariç, bu slug'a sahip BAŞKA bir post var mı?
        boolean existsBySlugAndIdNot(String slug, Long id);

        Optional<Post> findBySlugAndIsPublishedTrue(String slug);

        boolean existsBySlug(String slug);

        List<Post> findAllByIsPublishedFalseAndUpdatedAtBefore(LocalDateTime dateTime);

        // --------------

        // 1. Genel Akış (Filtreli / Filtresiz)
        @Query("SELECT p FROM Post p WHERE p.isPublished = true AND (:postType IS NULL OR p.postType = :postType)")
        Page<Post> findAllPublishedWithFilter(@Param("postType") PostType postType, Pageable pageable);

        // 2. Profil Sayfası (Filtreli / Filtresiz)
        @Query("SELECT p FROM Post p WHERE p.user.username = :username AND p.isPublished = true AND (:postType IS NULL OR p.postType = :postType)")
        Page<Post> findByUserUsernameAndPublishedWithFilter(@Param("username") String username,
                        @Param("postType") PostType postType, Pageable pageable);

        // 3. Kullanıcının Kendi Yazıları / Taslakları (Filtreli / Filtresiz)
        @Query("SELECT p FROM Post p WHERE p.user.username = :username " +
                        "AND (:isPublished IS NULL OR p.isPublished = :isPublished) " +
                        "AND (:postType IS NULL OR p.postType = :postType)")
        Page<Post> findMyOwnPostsWithFilter(@Param("username") String username,
                        @Param("isPublished") Boolean isPublished,
                        @Param("postType") PostType postType,
                        Pageable pageable);

}
