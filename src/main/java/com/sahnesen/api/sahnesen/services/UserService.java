package com.sahnesen.api.sahnesen.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.AccountStatus;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.UserRegisterRequest;
import com.sahnesen.api.sahnesen.response.AuthResponse;
import com.sahnesen.api.sahnesen.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        // 1. Güvenlik Kontrolü: Eskiden findAll kullanıyordun, şimdi exists
        // kullanıyoruz
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanımda.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Bu kullanıcı adı zaten alınmış.");
        }

        // 2. İnşa Süreci: Builder kullanımı (UserRegisterRequest burada kullanılıyor)
        User user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .username(request.getUsername().toLowerCase())
                .slug(request.getUsername().toLowerCase()) // Slug'ı username'e bağladık
                .password(passwordEncoder.encode(request.getPassword()))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        // 3. Veritabanına Yazma
        User savedUser = userRepository.save(user);

        // 4. Entity -> DTO Dönüşümü (Manüel Mapper)
        UserDTO userDto = UserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .surname(savedUser.getSurname())
                .slug(savedUser.getSlug())
                .profileImg(savedUser.getProfileImg())
                // Enum'ı String'e çeviriyoruz (.name() metodu ile)
                .role(savedUser.getRole().name())
                .build();

        // 5. Token Üretimi ve Yanıt İnsası
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .user(userDto)
                .token(token)
                .build();
    }
}
