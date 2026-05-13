package com.sahnesen.api.sahnesen.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

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

        // 1. Dosya Boş mu Kontrolü
        if (file.isEmpty()) {
            throw new RuntimeException("Boş dosya yükleyemezsiniz.");
        }

        // 2. Dosya Boyutu Kontrolü (örneğin 5MB'den büyük olmasın)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Dosya boyutu 5MB'den büyük olamaz.");
        }

        // 3. Dosya Türü Kontrolü (örneğin sadece resim dosyalarına izin verelim)

        String contentType = file.getContentType();
        if (contentType == null || !isSupportedContentType(contentType)) {
            throw new RuntimeException("Sadece JPEG, PNG ve WEBP formatları desteklenmektedir.");
        }

        try {
            // Önce post klasörünün var olduğundan emin olalım, güvenlik önlemi
            createPostFolder(postId);

            // 4. Dosya Adı Temizliği
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFileName);

            // Benzersiz isim: timestamp + rastgele sayı + uzantı
            String fileName = System.currentTimeMillis() + "_" + (int) (Math.random() * 1000) + extension;
            Path targetPath = root.resolve(postId.toString()).resolve(fileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/postImages/" + postId + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Görsel kaydedilirken bir hata oluştu: " + e.getMessage());
        }
    }

    // Yardımcı Metotlar
    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp");
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
