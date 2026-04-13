package com.sahnesen.api.sahnesen.request.socialMedia;

import com.sahnesen.api.sahnesen.enums.PlatformType;

import lombok.Data;

@Data
public class SocialMediaRequest {
    private PlatformType platform; // Instagram, GitHub, ...
    private String username;
    private String url; // Opisyonel username'den üretilebilir
    private Boolean isPublic = true; // Varsayılan olarak herkese açık
}
