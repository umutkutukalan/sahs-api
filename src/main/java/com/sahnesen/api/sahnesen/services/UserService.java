package com.sahnesen.api.sahnesen.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sahnesen.api.sahnesen.dto.UserDTO;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.AccountStatus;
import com.sahnesen.api.sahnesen.enums.ThemeType;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.request.UserLoginRequest;
import com.sahnesen.api.sahnesen.request.UserRegisterRequest;
import com.sahnesen.api.sahnesen.request.UserUpdateRequest;
import com.sahnesen.api.sahnesen.response.AuthResponse;
import com.sahnesen.api.sahnesen.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final FileStorageService fileStorageService;

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
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

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
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

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

    @Transactional
    public UserDTO updateMyProfile(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 1. Temel Alanlar
        if (request.getName() != null)
            user.setName(request.getName());
        if (request.getSurname() != null)
            user.setSurname(request.getSurname());
        if (request.getBio() != null)
            user.setBio(request.getBio());
        if (request.getMotto() != null)
            user.setMotto(request.getMotto());
        if (request.getProfileImg() != null)
            user.setProfileImg(request.getProfileImg());
        if (request.getCoverImg() != null)
            user.setCoverImg(request.getCoverImg());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getDistrict() != null)
            user.setDistrict(request.getDistrict());

        // 2. Username ve Slug (Önemli: Linkler değişeceği için dikkatli güncellenmeli)
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            String newUsername = request.getUsername().toLowerCase();
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(newUsername)) {
                    throw new RuntimeException("Bu kullanıcı adı zaten alınmış.");
                }
                user.setUsername(newUsername);
                user.setSlug(newUsername); // Username değişince slug da güncellenir
            }
        }

        // 3. Profesyonel Bilgiler (Embedded Update)
        if (request.getProfessional() != null) {
            var profReq = request.getProfessional();
            var prof = user.getProfessional();
            if (profReq.getJob() != null)
                prof.setJob(profReq.getJob());
            if (profReq.getAvailableFor() != null)
                prof.setAvailableFor(profReq.getAvailableFor());
            if (profReq.getPortfolioUrl() != null)
                prof.setPortfolioUrl(profReq.getPortfolioUrl());
            if (profReq.getSkills() != null)
                prof.setSkills(profReq.getSkills());
        }

        // 4. Tercihler (Embedded Update)
        if (request.getPreferences() != null) {
            var prefReq = request.getPreferences();
            var pref = user.getPreferences();
            if (prefReq.getTheme() != null)
                pref.setTheme(ThemeType.valueOf(prefReq.getTheme().toUpperCase()));
            if (prefReq.getIsPrivate() != null)
                pref.setPrivate(prefReq.getIsPrivate());
            if (prefReq.getAllowDirectMessages() != null)
                pref.setAllowDirectMessages(prefReq.getAllowDirectMessages());
            if (prefReq.getLanguage() != null)
                pref.setLanguage(prefReq.getLanguage());
        }

        return convertToDto(userRepository.save(user));
    }

    @Transactional
    public String updateUserImage(String username, MultipartFile file, String imageType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Dosyayı kaydet ve benzersiz bir dosya adı al
        String fileName = fileStorageService.storeFile(file);

        // Eski resmi sil (varsa) ve yeni dosya adını kullanıcıya kaydet
        if ("PROFILE".equals(imageType)) {
            if (user.getProfileImg() != null) {
                fileStorageService.deleteFile(user.getProfileImg());
            }
            user.setProfileImg(fileName);
        } else if ("COVER".equals(imageType)) {
            if (user.getCoverImg() != null) {
                fileStorageService.deleteFile(user.getCoverImg());
            }
            user.setCoverImg(fileName);
        }

        userRepository.save(user);
        return fileName;
    }
}
