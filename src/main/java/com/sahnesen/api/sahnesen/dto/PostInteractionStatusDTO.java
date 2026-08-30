package com.sahnesen.api.sahnesen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostInteractionStatusDTO {

    @JsonProperty("isLiked")
    private boolean isLiked;
    @JsonProperty("isShined")
    private boolean isShined;
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;
    private long likeCount;
    private long shineCount;
}