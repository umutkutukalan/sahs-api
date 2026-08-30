package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.CreateCollectionRequest;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.services.BookmarkCollectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interaction/bookmark-collections")
@RequiredArgsConstructor
public class BookmarkCollectionController {

    private final BookmarkCollectionService collectionService;

    // Kullanıcının klasörlerini getir
    @GetMapping
    public ResponseEntity<List<BookmarkCollection>> getUserCollections(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(collectionService.getUserCollections(principal.getName()));
    }

    // Kullanıcının kaydettiği postları sayfalı olarak getir
    @GetMapping("/posts")
    public ResponseEntity<Page<PostSummaryResponse>> getBookmarkedPosts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        return ResponseEntity.ok(collectionService.getBookmarkedPosts(principal.getName(), pageable));
    }

    // Yeni klasör oluştur
    @PostMapping
    public ResponseEntity<BookmarkCollection> createCollection(
            Principal principal,
            @RequestBody CreateCollectionRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(collectionService.createCollection(principal.getName(), request));
    }
}