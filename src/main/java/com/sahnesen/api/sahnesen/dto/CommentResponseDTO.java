package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponseDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    // Yazan kullanıcı bilgileri
    private String authorName;
    private String authorSurname;
    private String authorUsername;
    private String authorProfileImg;

    // Alt yanıtlar (Replies)
    private List<CommentResponseDTO> replies;
}