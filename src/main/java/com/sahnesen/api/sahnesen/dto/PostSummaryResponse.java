package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sahnesen.api.sahnesen.enums.PostType;

public record PostSummaryResponse(
                Long id,
                String title,
                String subtitle,
                String slug,
                String coverImage,
                PostType postType,
                List<String> tags,
                LocalDateTime createdAt,
                Long viewCount,
                LocalDateTime discussionEndsAt,
                Integer discussionDurationHours,
                String authorName,
                String authorSurname,
                String authorUsername,
                String authorProfileImg
// 💡 Content ALANI BURADA YOK!
) {
}
