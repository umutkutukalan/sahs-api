package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.PostReaction;
import com.sahnesen.api.sahnesen.enums.ReactionType;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    // Username üzerinden reaksiyon bulma
    Optional<PostReaction> findByUser_UsernameAndPostIdAndReactionType(String username, Long postId,
            ReactionType reactionType);

    // Username üzerinden reaksiyon varlık kontrolü
    boolean existsByUser_UsernameAndPostIdAndReactionType(String username, Long postId, ReactionType reactionType);

    long countByPostIdAndReactionType(Long postId, ReactionType reactionType);

    // Kullanıcının belirli bir reaksiyona sahip postlarını sayfalı getirir
    Page<PostReaction> findByUser_UsernameAndReactionType(String username, ReactionType reactionType,
            Pageable pageable);
}
