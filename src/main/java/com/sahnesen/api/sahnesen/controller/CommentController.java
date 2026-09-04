package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.CommentRequestDTO;
import com.sahnesen.api.sahnesen.dto.CommentResponseDTO;
import com.sahnesen.api.sahnesen.services.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Belirli bir yazıya ait fuaye mektuplarını ve yanıtları getir (Herkese açık)
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getPostComments(@PathVariable Long postId) {
        List<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    // Yazıya yeni bir fuaye mektubu veya alt yanıt ekle (Giriş yapmış kullanıcı)
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDTO request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CommentResponseDTO savedComment = commentService.addComment(principal.getName(), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }
}