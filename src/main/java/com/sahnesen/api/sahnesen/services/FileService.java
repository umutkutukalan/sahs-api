package com.sahnesen.api.sahnesen.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

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

}
