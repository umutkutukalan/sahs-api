package com.sahnesen.api.sahnesen.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final Path root = Paths.get("uploads/postImages");

    public void createPostFolder(Long postId) {
        try {
            Path postPath = root.resolve(postId.toString());
            if (!Files.exists(postPath)) {
                Files.createDirectories(postPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Post klasörü oluşturulamadı: " + e.getMessage());
        }
    }

    public String savePostImage(Long postId, MultipartFile file) {
        try {
            // Önce post klasörünün var olduğundan emin olalım, güvenlik önlemi
            createPostFolder(postId);

            // Dosya adını benzersiz yapalım
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetPath = root.resolve(postId.toString()).resolve(fileName);

            // Dosyayı kopyala
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Frontend'in erişebileceği URL'i döndürelim (Next.js buradan çekecek)
            return "/uploads/postImages/" + postId + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Görsel kaydedilemedi: " + e.getMessage());
        }
    }

}
