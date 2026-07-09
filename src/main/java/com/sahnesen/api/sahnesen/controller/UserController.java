package com.sahnesen.api.sahnesen.controller;

import com.sahnesen.api.sahnesen.services.FileStorageService;
import com.sahnesen.api.sahnesen.services.SocialMediaService;
import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(Principal principal) {
        if (principal == null) {
            // Eğer kullanıcı oturum açmadıysa direkt 401 fırlat ki Next.js login modalını
            // açsın
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // principal.getName() bize giriş yapan kullanıcının username bilgisini verir
        // (veya Spring Security'de neyi setlediysen, örn: email)
        UserDTO currentUser = userService.getMyProfileDetails(principal.getName());
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable String username) {
        UserDTO user = userService.getUserProfile(username);
        return ResponseEntity.ok(user);
    }

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

    @PostMapping("/me/profile-image")
    public ResponseEntity<String> updateProfileImage(@RequestParam("file") MultipartFile file, Principal principal) {
        String username = principal.getName();
        String fileName = userService.updateUserImage(username, file, "PROFILE");

        return ResponseEntity.ok(fileName);
    }

    // Kapak fotoğrafını güncellemek için ayrı endpoint
    @PostMapping("/me/cover-image")
    public ResponseEntity<String> updateCoverImage(@RequestParam("file") MultipartFile file, Principal principal) {
        String username = principal.getName();

        // Kapak resmi için benzer mantık
        String fileName = userService.updateUserImage(username, file, "COVER");

        return ResponseEntity.ok(fileName);
    }

    // ---

    @GetMapping("/{username}/social")
    public ResponseEntity<List<SocialMediaPlatform>> getPublicSocials(@PathVariable String username) {
        return ResponseEntity.ok(socialMediaService.getPublicPlatformsByUsername(username));
    }
}