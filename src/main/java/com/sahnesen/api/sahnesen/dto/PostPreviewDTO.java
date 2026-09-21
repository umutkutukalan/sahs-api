package com.sahnesen.api.sahnesen.dto;

import java.util.List;

public record PostPreviewDTO(Long id, String title, List<String> coverImages) {
}
