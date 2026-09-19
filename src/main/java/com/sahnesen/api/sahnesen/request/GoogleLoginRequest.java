package com.sahnesen.api.sahnesen.request;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String googleId;
    private String email;
    private String name;
    private String surname;
    private String picture;
    private String accessToken;
}
