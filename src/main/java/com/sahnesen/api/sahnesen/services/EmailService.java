package com.sahnesen.api.sahnesen.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // Örn: Frontend tarafında şifre yenileme sayfası:
        // https://sahnesen.co/reset-password?token=...
        String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("destek@sahnesen.com");
        message.setTo(toEmail);
        message.setSubject("Sahnesen - Şifre Sıfırlama Talebi");
        message.setText("Şifrenizi sıfırlamak için lütfen aşağıdaki bağlantıya tıklayın:\n\n" + resetUrl
                + "\n\nBu bağlantı 15 dakika geçerlidir.");

        mailSender.send(message);
    }
}
