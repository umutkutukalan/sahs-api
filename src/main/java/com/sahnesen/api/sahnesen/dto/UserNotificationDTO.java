package com.sahnesen.api.sahnesen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationDTO {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String slug;
    private String profileImg;
    @JsonProperty("isFollowing")
    private boolean isFollowing;
    private String role;
}
