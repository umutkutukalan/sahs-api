package com.sahnesen.api.sahnesen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.UserRole;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.UserRegisterRequest;
import com.sahnesen.api.sahnesen.response.UserRegisterResponse;
import com.sahnesen.api.sahnesen.services.UserService;
import com.sahnesen.api.sahnesen.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UserRegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRegisterRequest();
        request.setName("Umut");
        request.setSurname("Kutukalan");
        request.setEmail("kutukalan@gmail.com");
        request.setUsername("umutkutukalan");
        request.setPassword("password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // GIVEN (HAZIRLIK)
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .username(request.getUsername())
                .role(UserRole.USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("mocked_token");

        // WHEN (AKSIYON)
        UserRegisterResponse response = userService.register(request);

        // THEN (DOĞRULAMA)
        assertNotNull(response);
        assertEquals("mocked_token", response.getToken());
        assertEquals("umutkutukalan", response.getUser().getUsername());
        // Save metodu 1 kere çağrıldı mı?
        verify(userRepository, times(1)).save(any(User.class));

    }

}
