package com.sahnesen.api.sahnesen.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.PostInteractionStatusDTO;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.ReactionType;
import com.sahnesen.api.sahnesen.services.PostInteractionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostInteractionController {

    private final PostInteractionService interactionService;

    @PostMapping("/{postId}/reactions/toggle")
    public ResponseEntity<?> toggleReaction(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @RequestParam ReactionType reactionType) {
        boolean status = interactionService.toggleReaction(user, postId, reactionType);
        return ResponseEntity.ok(Map.of("reacted", status));
    }

    @PostMapping("/{postId}/bookmarks/toggle")
    public ResponseEntity<?> toggleBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @RequestParam(required = false) Long collectionId) {
        boolean status = interactionService.toggleBookmark(user, postId, collectionId);
        return ResponseEntity.ok(Map.of("bookmarked", status));
    }

    @GetMapping("/{postId}/interactions")
    public ResponseEntity<PostInteractionStatusDTO> getStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId) {
        return ResponseEntity.ok(interactionService.getInteractionStatus(user.getId(), postId));
    }
}
