package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;

import com.sahnesen.api.sahnesen.enums.PostType;

public record PostSummaryResponse(
                String title,
                String subtitle,
                String slug,
                String coverImage,
                PostType postType,
                LocalDateTime createdAt,
                Long viewCount,
                String authorName,
                String authorSurname,
                String authorUsername,
                String authorProfileImg
// 💡 Content ALANI BURADA YOK!
) {
}
