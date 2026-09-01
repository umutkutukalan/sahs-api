package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FollowDTO {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String profileImg;
    private LocalDateTime followedAt;
}
