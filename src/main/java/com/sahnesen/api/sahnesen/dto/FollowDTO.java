package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FollowDTO {
    private Long id;
    private String followerUsername;
    private String followingUsername;
    private LocalDateTime followedAt;
}
