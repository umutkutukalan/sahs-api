package com.sahnesen.api.sahnesen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TiptapContentExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. İlk Görseli Bulma (coverImage)
    public String extractFirstImage(String tiptapJson) {
        if (tiptapJson == null || tiptapJson.isBlank())
            return null;
        try {
            JsonNode root = objectMapper.readTree(tiptapJson);
            return findFirstImageUrl(root);
        } catch (Exception e) {
            return null;
        }
    }

    private String findFirstImageUrl(JsonNode node) {
        if (node.has("type") && node.get("type").asText().equals("image")) {
            if (node.has("attrs") && node.get("attrs").has("src")) {
                return node.get("attrs").get("src").asText();
            }
        }
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                String url = findFirstImageUrl(child);
                if (url != null)
                    return url;
            }
        }
        return null;
    }

    // 2. Subtitle Bulma (İlk H2 veya İlk Paragrafın Başı)
    public String extractSubtitle(String tiptapJson) {
        if (tiptapJson == null || tiptapJson.isBlank())
            return null;
        try {
            JsonNode root = objectMapper.readTree(tiptapJson);

            // Önce İlk H2'yi Ara
            String h2Text = findFirstH2(root);
            if (h2Text != null && !h2Text.isBlank()) {
                return h2Text;
            }

            // H2 yoksa İlk Paragrafın ilk 150 Karakterini Al
            String firstParagraph = findFirstParagraph(root);
            if (firstParagraph != null && !firstParagraph.isBlank()) {
                return firstParagraph.length() > 150
                        ? firstParagraph.substring(0, 147) + "..."
                        : firstParagraph;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String findFirstH2(JsonNode node) {
        if (node.has("type") && node.get("type").asText().equals("heading")) {
            if (node.has("attrs") && node.get("attrs").has("level") && node.get("attrs").get("level").asInt() == 2) {
                return extractTextFromNode(node);
            }
        }
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                String text = findFirstH2(child);
                if (text != null)
                    return text;
            }
        }
        return null;
    }

    private String findFirstParagraph(JsonNode node) {
        if (node.has("type") && node.get("type").asText().equals("paragraph")) {
            return extractTextFromNode(node);
        }
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                String text = findFirstParagraph(child);
                if (text != null)
                    return text;
            }
        }
        return null;
    }

    private String extractTextFromNode(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        if (node.has("text")) {
            sb.append(node.get("text").asText());
        }
        if (node.has("content") && node.get("content").isArray()) {
            for (JsonNode child : node.get("content")) {
                sb.append(extractTextFromNode(child));
            }
        }
        return sb.toString();
    }
}
