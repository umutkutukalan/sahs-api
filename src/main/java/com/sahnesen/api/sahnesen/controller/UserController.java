package com.sahnesen.api.sahnesen.controller;

import com.sahnesen.api.sahnesen.services.SocialMediaService;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.entities.SocialMediaPlatform;
import com.sahnesen.api.sahnesen.request.UserUpdateRequest;
import com.sahnesen.api.sahnesen.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SocialMediaService socialMediaService;
    private final UserService userService;

    // Kendi profilini güncelle
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@Valid @RequestBody UserUpdateRequest updateRequest,
            Principal principal) {

        if (principal == null) {
            // Eğer hala null geliyorsa, güvenlik katmanında bir sızıntı var demektir
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO updatedUser = userService.updateMyProfile(principal.getName(), updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // ---
    
    @GetMapping("/{username}/social")
    public ResponseEntity<List<SocialMediaPlatform>> getPublicSocials(@PathVariable String username) {
        return ResponseEntity.ok(socialMediaService.getPublicPlatformsByUsername(username));
    }
}