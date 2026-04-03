package com.sahnesen.api.sahnesen.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank(message = "Kullanıcı adı veya e-posta boş bırakılamaz")
    private String identifier; // Bu alan hem email hem username yerine geçer
    @NotBlank(message = "Şifre alanı boş bırakılamaz")
    private String password;
}
