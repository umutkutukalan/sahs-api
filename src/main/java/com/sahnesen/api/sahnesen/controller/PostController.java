package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

}
