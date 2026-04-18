package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.response.PostResponse;
import com.sahnesen.api.sahnesen.services.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Yeni bir post (Sahne) oluştur.
     * Sadece giriş yapmış kullanıcılar kendi adlarına post oluşturabilir.
     */
    @PostMapping("/me")
    public ResponseEntity<PostResponse> createMyPost(
            @Valid @RequestBody PostRequestDTO request,
            Principal principal) {
        PostResponse savedPost = postService.createPost(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    // Sadece giriş yapan kullanıcının kendi (taslaklar dahil) tüm postlarını
    // görmesi için

    @GetMapping("/me")
    public ResponseEntity<Page<PostResponse>> getMyAllPosts(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(postService.getMyOwnPosts(principal.getName(), pageable));
    }

    @PutMapping("/me/{postId}")
    public ResponseEntity<PostResponse> updateMyPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDTO request,
            Principal principal) {
        PostResponse updatedPost = postService.updatePost(principal.getName(), postId, request);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/me/{postId}")
    public ResponseEntity<Void> deleteMyPost(@PathVariable Long postId, Principal principal) {
        postService.deletePost(principal.getName(), postId);
        return ResponseEntity.noContent().build();
    }

    // ----

    // Genel Akış (Herkes görebilir)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPublishedPosts(pageable));
    }

    // Kullanıcıya Özel Akış (Profil sayfası için)
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable String username,
            Pageable pageable) {
        return ResponseEntity.ok(postService.getUserPosts(username, pageable));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostDetail(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostWithViewCount(slug));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingPosts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(postService.getTopPosts(limit));
    }

}
