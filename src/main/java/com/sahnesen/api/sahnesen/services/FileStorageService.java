package com.sahnesen.api.sahnesen.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Klasör oluşturulamadı.", e);
        }
    }

    public String storeFile(MultipartFile file, String subDir) {
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new RuntimeException("Sadece JPEG, PNG ve WebP formatında dosyalar yüklenebilir.");
        }

        // Alt klasör oluşturma
        Path targetDir = this.rootLocation.resolve(subDir).normalize();
        try {
            Files.createDirectories(targetDir);
        } catch (Exception e) {
            throw new RuntimeException("Alt klasör oluşturulamadı.", e);
        }

        // Dosya adını temizleyelim
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetLocation = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Dosya depolanamadı.", e);
        }
    }

    public void deleteFile(String fullPath) {
        try {
            Path filePath = this.rootLocation.resolve(fullPath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Dosya silinemedi. " + fullPath, e);
        }
    }

}
