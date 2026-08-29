package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.PostReaction;
import com.sahnesen.api.sahnesen.enums.ReactionType;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    Optional<PostReaction> findByUserIdAndPostIdAndReactionType(Long userId, Long postId, ReactionType reactionType);

    boolean existsByUserIdAndPostIdAndReactionType(Long userId, Long postId, ReactionType reactionType);

    long countByPostIdAndReactionType(Long postId, ReactionType reactionType);
}
