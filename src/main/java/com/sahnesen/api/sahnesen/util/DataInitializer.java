package com.sahnesen.api.sahnesen.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.services.PostService;

@Configuration
public class DataInitializer {
    @Bean
    @Profile("!test") // ÖNEMLİ: Testler çalışırken bu veri ekleme işini yapmasın!
    CommandLineRunner runner(PostService postService) {
        return args -> {
            // Sadece DB boşsa ekle gibi bir mantık da kurabilirsin
            PostRequestDTO dto = new PostRequestDTO();
            dto.setTitle("Redis Test Yazısı");
            dto.setContent("Bu yazı Redis önbellekleme testidir.");
            dto.setPostType(PostType.BLOG);
            dto.setCoverImage("https://example.com/cover.jpg");
            dto.setPublished(true);

            // Not: postService.createPost metodun kullanıcı adı bekliyorsa
            // DB'de olan bir kullanıcı adını buraya yazmalısın.
            postService.createPost("kutukalan", dto);
        };
    }

}