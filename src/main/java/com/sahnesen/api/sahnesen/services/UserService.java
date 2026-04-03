package com.sahnesen.api.sahnesen.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.AccountStatus;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.UserLoginRequest;
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

        // 4. Entity -> DTO Dönüşümü
        UserDTO userDto = convertToDto(savedUser);

        // 5. Token Üretimi ve Yanıt İnsası
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .user(userDto)
                .token(token)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        // 1. Kullanıcıyı identifier (email veya username) üzerinden bul
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                        .orElseThrow(() -> new RuntimeException("Kullanıcı adı/e-posta veya şifre hatalı")));

        // 2. Şifre kontrolü
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Kullanıcı adı/e-posta veya şifre hatalı");
        }

        // 3. Token üretimi
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 4. UserDTO'ya dönüştür ve Response oluştur
        UserDTO userDTO = convertToDto(user);

        return AuthResponse.builder()
                .user(userDTO)
                .token(token)
                .build();
    }

    // Manuel Entity -> DTO dönüşüm metodu
    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .slug(user.getSlug())
                .profileImg(user.getProfileImg())
                .role(user.getRole().name()) // Role Enum ise .name() ile String'e çeviriyoruz
                .build();
    }

}
