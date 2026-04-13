package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.entities.SocialMediaPlatform;
import com.sahnesen.api.sahnesen.request.socialMedia.SocialMediaRequest;
import com.sahnesen.api.sahnesen.services.SocialMediaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/me/social")
@RequiredArgsConstructor
public class UserSocialMediaController {

    private final SocialMediaService socialMediaService;

    /** Kendi profilime yeni bir sosyal medya platformu ekler */
    @PostMapping
    public ResponseEntity<SocialMediaPlatform> addPlatform(@Valid @RequestBody SocialMediaRequest request,
            Principal principal) {
        // Principal.getName() bize username'i verecek (Auth kurgumuza göre)
        SocialMediaPlatform savedPlatform = socialMediaService.addPlatform(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlatform);
    }

    /** Kendi eklediğim tüm sosyal medya platformlarını getirir */
    @GetMapping
    public ResponseEntity<List<SocialMediaPlatform>> getMySocials(Principal principal) {
        return ResponseEntity.ok(socialMediaService.getMyPlatform(principal.getName()));
    }

    @DeleteMapping("/{platformId}")
    public ResponseEntity<Void> deleteMySocial(@PathVariable Long platformId, Principal principal) {
        socialMediaService.deletePlatform(principal.getName(), platformId);
        return ResponseEntity.noContent().build();
    }

}
