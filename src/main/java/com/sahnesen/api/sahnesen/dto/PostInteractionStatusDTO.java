package com.sahnesen.api.sahnesen.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostInteractionStatusDTO {
    private boolean isLiked;
    private boolean isShined;
    private boolean isBookmarked;
    private long likeCount;
    private long shineCount;
}