package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.PostInteractionStatusDTO;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.enums.ReactionType;
import com.sahnesen.api.sahnesen.services.PostInteractionService;

import io.lettuce.core.GeoArgs.Sort;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interaction/posts")
@RequiredArgsConstructor
public class PostInteractionController {

    private final PostInteractionService interactionService;

    @PostMapping("/{postId}/reactions/toggle")
    public ResponseEntity<?> toggleReaction(
            Principal principal,
            @PathVariable Long postId,
            @RequestParam ReactionType reactionType) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean status = interactionService.toggleReaction(principal.getName(), postId, reactionType);
        return ResponseEntity.ok(Map.of("reacted", status));
    }

    @PostMapping("/{postId}/bookmarks/toggle")
    public ResponseEntity<?> toggleBookmark(
            Principal principal,
            @PathVariable Long postId,
            @RequestParam(required = false) Long collectionId) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean status = interactionService.toggleBookmark(principal.getName(), postId, collectionId);
        return ResponseEntity.ok(Map.of("bookmarked", status));
    }

    @GetMapping("/{postId}/interactions")
    public ResponseEntity<PostInteractionStatusDTO> getStatus(
            Principal principal,
            @PathVariable Long postId,
            @RequestParam ReactionType targetShineType) { // Hangi moda ait parlatma durumu soruluyorsa
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(interactionService.getInteractionStatus(principal.getName(), postId, targetShineType));
    }

    @GetMapping("/liked")
    public ResponseEntity<Page<PostSummaryResponse>> getLikedPosts(
            Principal principal,
            @PageableDefault(size = 5, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(interactionService.getLikedPosts(principal.getName(), pageable));
    }
}