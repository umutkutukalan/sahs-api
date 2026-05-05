package com.sahnesen.api.sahnesen.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.FollowDTO;
import com.sahnesen.api.sahnesen.entities.Follow;
import com.sahnesen.api.sahnesen.services.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 🚀 Bir kullanıcıyı takip et
     * followerUsername bilgisini JWT token'dan (SecurityContext) çekiyoruz.
     */
    @PostMapping("/{followingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowDTO> followUser(@PathVariable Long followingId,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String followerUsername = principal.getName();
        Follow follow = followService.followUser(followerUsername, followingId);
        FollowDTO followDTO = new FollowDTO();
        followDTO.setId(follow.getId());
        followDTO.setFollowerUsername(follow.getFollower().getUsername());
        followDTO.setFollowingUsername(follow.getFollowing().getUsername());
        followDTO.setFollowedAt(follow.getCreatedAt());
        return ResponseEntity.ok(followDTO);
    }

     /**
     * Takibi bırak
     */
    @DeleteMapping("/{followingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollowUser(@PathVariable Long followingId,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String followerUsername = principal.getName();
        followService.unfollowUser(followerUsername, followingId);
        return ResponseEntity.ok().build();
    }

    /**
     * 📊 Takipçi ve takip edilen sayılarını getir
     * Bu endpoint public olabilir, profil sayfasında herkes görebilir.
     */
    @GetMapping("/stats/{username}")
    public ResponseEntity<Map<String, Long>> getFollowStats(@PathVariable String username) {
        return ResponseEntity.ok(followService.getFollowStats(username));
    }
}
