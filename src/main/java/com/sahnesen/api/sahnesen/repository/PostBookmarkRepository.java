package com.sahnesen.api.sahnesen.repository;

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

    Page<PostBookmark> findByCollectionId(Long collectionId, Pageable pageable);

    // Doğrudan username üzerinden kullanıcının tüm kaydedilenlerini sayfalı getir
    Page<PostBookmark> findByCollection_User_Username(String username, PostType postType, Pageable pageable);
}