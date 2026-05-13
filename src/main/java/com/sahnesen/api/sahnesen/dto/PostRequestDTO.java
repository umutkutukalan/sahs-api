package com.sahnesen.api.sahnesen.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sahnesen.api.sahnesen.enums.PostType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequestDTO {

    @NotNull(message = "Lütfen içeriğiniz için bir tür seçiniz.")
    private PostType postType; // Yazı türü (BLOG, STUDY, PROJECT, TUTORIAL, OPINION)

    @NotBlank(message = "Başlık boş olamaz.")
    @Size(min = 3, max = 150, message = "Başlık 3 ile 150 karakter arasında olmalıdır.")
    private String title;

    @NotBlank(message = "İçerik boş olamaz.")
    private JsonNode content; // Tiptap JSON String

    private String coverImage; // Opsiyonel Kapak Fotoğrafı URL'i

    private boolean isPublished = false; // Direkt yayınlamak mı isteriz yoksa taslak mı kalsın?

}
