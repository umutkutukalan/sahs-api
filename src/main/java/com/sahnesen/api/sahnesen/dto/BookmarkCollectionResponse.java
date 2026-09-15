package com.sahnesen.api.sahnesen.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkCollectionResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private boolean isDefault;
    private List<PostPreviewDTO> contents;
    private long itemCount;
}
