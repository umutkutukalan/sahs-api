package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.entities.Post;
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
    public ResponseEntity<Post> createMyPost(
            @Valid @RequestBody PostRequestDTO request,
            Principal principal) {
        Post savedPost = postService.createPost(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    // Sadece giriş yapan kullanıcının kendi (taslaklar dahil) tüm postlarını
    // görmesi için

    @GetMapping("/me")
    public ResponseEntity<Page<Post>> getMyAllPosts(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(postService.getMyOwnPosts(principal.getName(), pageable));
    }

    // ----

    // 1. Genel Akış (Herkes görebilir)
    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPublishedPosts(pageable));
    }

    // 2. Kullanıcıya Özel Akış (Profil sayfası için)
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<Post>> getUserPosts(
            @PathVariable String username,
            Pageable pageable) {
        return ResponseEntity.ok(postService.getUserPosts(username, pageable));
    }

}
