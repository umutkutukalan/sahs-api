package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.enums.PostType;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    // Kullanıcının username'i ve postId'sine göre bookmark bul
    Optional<PostBookmark> findByCollection_User_UsernameAndPostId(String username, Long postId);

    // Kullanıcının username'i ve postId'sine göre bookmark var mı kontrol et
    boolean existsByCollection_User_UsernameAndPostId(String username, Long postId);

    boolean existsByCollectionIdAndPostId(Long collectionId, Long postId);

    Page<PostBookmark> findByCollectionId(Long collectionId, Pageable pageable);

    Optional<PostBookmark> findByCollectionIdAndPostId(Long collectionId, Long postId);

    // Kullanıcının varsayılan koleksiyonundaki belirli bir postu bulmak için
    Optional<PostBookmark> findByCollection_User_UsernameAndCollection_IsDefaultTrueAndPostId(String username,
            Long postId);

    // Doğrudan username üzerinden kullanıcının tüm kaydedilenlerini sayfalı getir
    Page<PostBookmark> findByCollection_User_Username(String username, PostType postType, Pageable pageable);

    // Kullanıcının bu post için sahip olduğu TÜM bookmark kayıtlarını getirir
    // (post birden fazla koleksiyona kaydedilmiş olabileceği için Optional değil
    // List)
    List<PostBookmark> findAllByCollection_User_UsernameAndPostId(String username, Long postId);

}