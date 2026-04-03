package com.sahnesen.api.sahnesen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sahnesen.api.sahnesen.request.UserRegisterRequest;
import com.sahnesen.api.sahnesen.response.UserRegisterResponse;
import com.sahnesen.api.sahnesen.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService; // final olmalı ki LOMBOK enjekte etsin

    // 🔐 Güvenli JWT Cookie oluşturma metodu
    private void setSecureJwtCookie(HttpServletResponse response, String token) {
        System.out.println("Cookie ayarlanıyor - Token: " + token.substring(0, 20) + "...");

        Cookie jwtCookie = new Cookie("authToken", token);
        jwtCookie.setHttpOnly(true); // JavaScript ile erişilemez (XSS koruması)
        jwtCookie.setSecure(false); // Development için false, production'da true
        jwtCookie.setPath("/"); // Tüm path'lerde geçerli
        jwtCookie.setMaxAge(24 * 60 * 60); // 24 saat

        // ⚠️ KRITIK: Cross-port sharing için domain ayarla
        jwtCookie.setDomain("localhost"); // Hem 5173 hem 8080 için

        response.addCookie(jwtCookie);

        System.out.println("Cookie set edildi - HttpOnly: true, Secure: false, Path: /, MaxAge: " + (24 * 60 * 60)
                + ", Domain: localhost");

        // SameSite için manuel header (Java Cookie API'si SameSite desteklemiyor)
        String sameSiteCookie = String.format(
                "authToken=%s; HttpOnly; Path=/; Max-Age=%d; Domain=localhost; SameSite=Lax",
                token,
                24 * 60 * 60);
        response.addHeader("Set-Cookie", sameSiteCookie);

        System.out.println("Manuel header eklendi: " + sameSiteCookie.substring(0, 70) + "...");
    }

    @PostMapping("/register")
    // @Valid ekledik ki Request içindeki kurallar çalışsın
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request, HttpServletResponse response) {

        // try-catch yok! Hata olursa otomatik olarak GlobalExceptionHandler'a gider.
        UserRegisterResponse registerResponse = userService.register(request);

        // Cookie olarak token'ı ayarla
        setSecureJwtCookie(response, registerResponse.getToken());

        // Güvenlik için token'ı body'den temizliyoruz, sadece Cookie'de kalıyor.
        registerResponse.setToken(null);

        return ResponseEntity.ok(registerResponse);
    }

}
