package com.sahnesen.api.sahnesen.response;

import java.time.LocalDateTime;

import com.sahnesen.api.sahnesen.enums.PostType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String content;
    private String coverImage;
    private PostType postType;
    private LocalDateTime createdAt;

    // Yazar Bilgileri (Sadece gerekli olanlar)
    private String authorName;
    private String authorSurname;
    private String authorUsername;
    private String authorProfileImg;
}
