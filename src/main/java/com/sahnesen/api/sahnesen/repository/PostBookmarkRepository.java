package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.PostBookmark;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    Optional<PostBookmark> findByCollectionUserIdAndPostId(Long userId, Long postId);

    boolean existsByCollectionUserIdAndPostId(Long userId, Long postId);

    Page<PostBookmark> findByCollectionId(Long collectionId, Pageable pageable);

    Page<PostBookmark> findByCollectionUserId(Long userId, Pageable pageable);
}
