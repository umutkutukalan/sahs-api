package com.sahnesen.api.sahnesen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotBlank(message = "Yorum/mektup içeriği boş olamaz.")
    @Size(max = 2000, message = "İçerik en fazla 2000 karakter olmalıdır.")
    private String content;

    // Eğer bu bir alt yanıtsa, hangi ana yoruma yazıldığı belirtilir (Opsiyonel)
    private Long parentId;
}