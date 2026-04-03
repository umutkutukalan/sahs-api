package com.sahnesen.api.sahnesen.dto;

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
    private String slug;
    private String profileImg;
    private String role;
}
