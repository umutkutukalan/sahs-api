package com.sahnesen.api.sahnesen.controller;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.request.UserRegisterRequest;
import com.sahnesen.api.sahnesen.response.AuthResponse;
import com.sahnesen.api.sahnesen.services.UserService;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Security filtrelerini şimdilik geçiyoruz
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Nesneleri JSON'a dönüştürmek için

    @MockitoBean
    private UserService userService; // UserService'i mocklamak için

    private UserRegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new UserRegisterRequest();
        validRequest.setName("Umut");
        validRequest.setSurname("Kutukalan");
        validRequest.setEmail("umut@sahnesen.com");
        validRequest.setUsername("umutkutukalan");
        validRequest.setPassword("password123");
    }

    @Test
    void shouldRegisterSuccesfully() throws Exception {
        // GIVEN
        UserDTO userDTO = UserDTO.builder()
                .username("umutkutukalan")
                .email("umut@sahnesen.com")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .user(userDTO)
                .token("mock-jwt-token")
                .build();

        when(userService.register(any(UserRegisterRequest.class))).thenReturn(mockResponse);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                // .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("umutkutukalan"))
                .andExpect(cookie().exists("authToken"));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        // GIVEN (Hatalı veri: Geçersiz email ve kısa şifre)
        UserRegisterRequest invalidRequest = new UserRegisterRequest();
        invalidRequest.setEmail("gecersiz-email");
        invalidRequest.setPassword("123");

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                // .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.name").value("İsim alanı boş bırakılamaz"));
    }

}
