
package com.sahnesen.api.sahnesen.response;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.sahnesen.api.sahnesen.enums.PostType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor //Jackson için boş constructor
@AllArgsConstructor  //Builder için tüm argümanları içeren constructor
public class PostResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String coverImage;
    private PostType postType;
    private LocalDateTime createdAt;
    private Long viewCount; // Görüntülenme sayısı, Redis'ten çekilecek

    // Yazar Bilgileri (Sadece gerekli olanlar)
    private String authorName;
    private String authorSurname;
    private String authorUsername;
    private String authorProfileImg;
}