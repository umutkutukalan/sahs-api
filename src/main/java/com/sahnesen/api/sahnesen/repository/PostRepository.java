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

        // 4. Takip Edilenlerin İçerikleri (Yenilikler Akışı)
        @Query("SELECT p FROM Post p WHERE p.user.id IN " +
                        "(SELECT f.following.id FROM Follow f WHERE f.follower.username = :username) " +
                        "AND p.isPublished = true AND (:postType IS NULL OR p.postType = :postType)")
        Page<Post> findFollowingPostsWithFilter(@Param("username") String username,
                        @Param("postType") PostType postType, Pageable pageable);

        // 4 Katmanlı Ağırlıklı Arama (Weighted Search)
        // 1. Etiket eşleşmesi: En yüksek puan (+10)
        // 2. Başlık eşleşmesi: Yüksek puan (+5)
        // 3. Alt metin / Özet eşleşmesi: Orta puan (+2)
        // 4. Gövde eşleşmesi: Düşük puan (+1)
        @Query(value = "SELECT DISTINCT p.*, " +
                        "(CASE WHEN EXISTS (SELECT 1 FROM post_tags pt JOIN tags t ON pt.tag_id = t.id WHERE pt.post_id = p.id AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) THEN 10 ELSE 0 END + "
                        +
                        " CASE WHEN LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 5 ELSE 0 END + " +
                        " CASE WHEN LOWER(p.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 2 ELSE 0 END + " +
                        " CASE WHEN LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 1 ELSE 0 END) AS relevance_score "
                        +
                        "FROM posts p " +
                        "WHERE p.is_published = true AND (" +
                        "   EXISTS (SELECT 1 FROM post_tags pt JOIN tags t ON pt.tag_id = t.id WHERE pt.post_id = p.id AND LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR "
                        +
                        "   LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "   LOWER(p.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "   LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                        ") " +
                        "ORDER BY relevance_score DESC, p.created_at DESC", countQuery = "SELECT COUNT(DISTINCT p.id) FROM posts p "
                                        +
                                        "LEFT JOIN post_tags pt ON p.id = pt.post_id " +
                                        "LEFT JOIN tags t ON pt.tag_id = t.id " +
                                        "WHERE p.is_published = true AND (" +
                                        "   LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                                        "   LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                                        "   LOWER(p.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                                        "   LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                                        ")", nativeQuery = true)
        Page<Post> searchPostsWeighted(@Param("keyword") String keyword, Pageable pageable);

}
