package com.sahnesen.api.sahnesen.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sahnesen.api.sahnesen.enums.PostType;

public record PostSummaryResponse(
                Long id,
                String publicId,
                String title,
                String subtitle,
                String slug,
                List<String> coverImages,
                PostType postType,
                List<String> tags,
                @JsonProperty("isPublished") boolean isPublished,
                @JsonProperty("isArchived") boolean isArchived,
                LocalDateTime createdAt,
                Long viewCount,
                LocalDateTime discussionEndsAt,
                Integer discussionDurationHours,
                String authorName,
                String authorSurname,
                String authorUsername,
                String authorProfileImg) {
        public boolean isPublished() {
                return isPublished;
        }

        public boolean isArchived() {
                return isArchived;
        }
}
