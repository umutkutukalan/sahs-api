package com.sahnesen.api.sahnesen.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "İsim alanı boş bırakılamaz")
    private String name;

    @NotBlank(message = "Soyisim alanı boş bırakılamaz")
    private String surname;

    @NotBlank(message = "E-posta alanı boş bırakılamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String mail;

    @NotBlank(message = "Şifre alanı boş bırakılamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;
}
