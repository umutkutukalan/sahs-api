package com.sahnesen.api.sahnesen.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.SocialMediaPlatform;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.SocialMediaPlatformRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.socialMedia.SocialMediaRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialMediaService {

    private final SocialMediaPlatformRepository socialMediaPlatformRepository;

    private final UserRepository userRepository;

    public SocialMediaPlatform addPlatform(String username, SocialMediaRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunumadı."));
        if (socialMediaPlatformRepository.existsByUser_UsernameAndPlatform(username, request.getPlatform())) {
            throw new RuntimeException("Bu platform zaten profilinizde mevcut." + request.getPlatform());
        }

        SocialMediaPlatform platform = SocialMediaPlatform.builder()
                .user(user)
                .platform(request.getPlatform())
                .username(request.getUsername())
                .isPublic(request.getIsPublic())
                .build();

        if (request.getUrl() == null || request.getUrl().isBlank()) {
            platform.updateUrlByUsername();
        } else {
            platform.setUrl(request.getUrl());
        }

        return socialMediaPlatformRepository.save(platform);
    }

    @Transactional(readOnly = true)
    public List<SocialMediaPlatform> getMyPlatform(String username) {
        return socialMediaPlatformRepository.findAllByUser_Username(username);
    }

    @Transactional
    public void deletePlatform(String username, Long platformId) {
        SocialMediaPlatform platform = socialMediaPlatformRepository.findById(platformId)
                .orElseThrow(() -> new RuntimeException("Platform bulunamadı.."));
        if (!platform.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu işlem için yetkiniz yok.");
        }
        socialMediaPlatformRepository.delete(platform);
    }

    // ---

    @Transactional(readOnly = true)
    public List<SocialMediaPlatform> getPublicPlatformsByUsername(String username) {
        return socialMediaPlatformRepository.findAllByUser_UsernameAndIsPublicTrue(username);
    }

}
