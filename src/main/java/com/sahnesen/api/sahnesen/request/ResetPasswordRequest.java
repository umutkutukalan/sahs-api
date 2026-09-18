package com.sahnesen.api.sahnesen.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "Sıfırlama tokeni boş olamaz.")
    private String token;

    @NotBlank(message = "Yeni şifre boş olamaz.")
    @Size(min = 6, message = "Yeni şifre en az 6 karakter olmalıdır.")
    private String newPassword;
}
