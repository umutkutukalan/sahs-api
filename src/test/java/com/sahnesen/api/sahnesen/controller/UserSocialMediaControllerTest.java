package com.sahnesen.api.sahnesen.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sahnesen.api.sahnesen.entities.SocialMediaPlatform;
import com.sahnesen.api.sahnesen.enums.PlatformType;
import com.sahnesen.api.sahnesen.request.socialMedia.SocialMediaRequest;
import com.sahnesen.api.sahnesen.services.SocialMediaService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserSocialMediaController.class)
public class UserSocialMediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SocialMediaService socialMediaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAddSocialMediaPlatformSuccessfully() throws Exception {
        // GIVEN
        SocialMediaRequest request = new SocialMediaRequest();
        request.setPlatform(PlatformType.GITHUB);
        request.setUsername("umutkutukalan");

        SocialMediaPlatform savedPlatform = SocialMediaPlatform.builder()
                .id(1L)
                .platform(PlatformType.GITHUB)
                .username("umutkutukalan")
                .url("https://github.com/umutkutukalan")
                .build();

        // Servis katmanını simüle ediyoruz
        when(socialMediaService.addPlatform(eq("umutkutukalan"), any(SocialMediaRequest.class)))
                .thenReturn(savedPlatform);

        // WHEN & THEN
        mockMvc.perform(post("/api/users/me/social")
                .with(csrf())
                .with(user("umutkutukalan")) // Principal.getName() -> "umutkutukalan"
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.platform").value("GITHUB"))
                .andExpect(jsonPath("$.url").value("https://github.com/umutkutukalan"));
    }

}
