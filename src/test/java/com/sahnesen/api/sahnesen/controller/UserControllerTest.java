package com.sahnesen.api.sahnesen.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sahnesen.api.sahnesen.config.SecurityConfig;
import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.request.UserUpdateRequest;
import com.sahnesen.api.sahnesen.services.UserService;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // SecurityConfig'i test konteynerine dahil ediyoruz
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Nesneleri JSON'a dönüştürmek için

    @MockitoBean
    private UserService userService; // UserService'i mocklamak için

    private UserUpdateRequest validUpdateRequest;

    @Test
    @WithMockUser(username = "umutkutukalan")
    void shouldUpdateProfileSuccessfully() throws Exception {
        // GIVEN
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setName("Umut Güncel");
        updateRequest.setBio("Yeni biyografi buraya");

        UserDTO updatedUserDTO = UserDTO.builder()
                .name("Umut Güncel")
                .username("umutkutukalan")
                .email("umut@sahnesen.com")
                .build();

        // UserService'in bu isteğe nasıl cevap vereceğini taklit ediyoruz
        when(userService.updateMyProfile(eq("umutkutukalan"), any(UserUpdateRequest.class)))
                .thenReturn(updatedUserDTO);

        // WHEN & THEN
        mockMvc.perform(put("/api/users/me").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Umut Güncel"))
                .andExpect(jsonPath("$.username").value("umutkutukalan"));
    }

}
