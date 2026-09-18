package com.sahnesen.api.sahnesen.dto;

import com.sahnesen.api.sahnesen.entities.model.UserMetrics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String bio;
    private String slug;
    private String profileImg;
    private String coverImg;
    private String role;
    private UserMetrics metrics;
}
