package com.sahnesen.api.sahnesen.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.response.PostResponse;
import com.sahnesen.api.sahnesen.services.PostService;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PostService postService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldCreatePostSuccessfully() throws Exception {
                // GIVEN
                PostRequestDTO request = new PostRequestDTO();
                request.setTitle("Test Başlığı");
                request.setContent(("{\"type\": \"doc\", \"content\": []}"));
                request.setPostType(PostType.STUDY);
                request.setPublished(true);

                PostResponse savedPost = PostResponse.builder()
                                .id(1L)
                                .title("Test Başlığı")
                                .slug("test-basligi")
                                .postType(PostType.STUDY)
                                .authorName("Umut")
                                .authorUsername("umutkutukalan")
                                .build();

                when(postService.createPost(eq("umutkutukalan"), any(PostRequestDTO.class)))
                                .thenReturn(savedPost);

                // WHEN & THEN
                mockMvc.perform(post("/api/posts/me")
                                .with(csrf())
                                .with(user("umutkutukalan"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.slug").value("test-basligi"))
                                .andExpect(jsonPath("$.postType").value("STUDY"))
                                .andExpect(jsonPath("$.authorName").value("Umut"))
                                .andExpect(jsonPath("$.authorUsername").value("umutkutukalan"));
        }
}