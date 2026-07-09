package com.sahnesen.api.sahnesen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String slug;
    private String profileImg;
    private String coverImg;
    private String bio;
    private String motto;
    private String city;
    private String district;
    private String role;
}
