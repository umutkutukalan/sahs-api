package com.sahnesen.api.sahnesen.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.FollowRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Her testten sonra veritabanını temizlemek için
public class FollowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private User targetUser;
    private final String FOLLOWER_USERNAME = "kutukalanumut";

    @BeforeEach
    void setUp() {
        // Test için hedef kullanıcıyı oluştur
        targetUser = new User();
        targetUser.setUsername("kutukalan");
        targetUser.setSlug("kutukalan");
        targetUser.setEmail("kutukalan@gmail.com");
        targetUser.setPassword("p4ssword");
        targetUser = userRepository.save(targetUser);

        // Takipçi kullanıcı ( Giriş yapan kişi )
        User follower = new User();
        follower.setUsername(FOLLOWER_USERNAME);
        follower.setSlug(FOLLOWER_USERNAME);
        follower.setEmail(FOLLOWER_USERNAME + "@gmail.com");
        follower.setPassword("p4ssword");
        follower = userRepository.save(follower);

        // Redis'teki eski test verilerini temizleyelim
        redisTemplate.delete("user:followers:count:" + targetUser.getId());
    }

    @Test
    @DisplayName("Başarılı Takip İşlemi - DB ve Redis Kontrolü")
    @WithMockUser(username = FOLLOWER_USERNAME) // JWT varmış gibi davranır
    void followUser_Success() throws Exception {

        // 1. İşlem: POST isteği at
        mockMvc.perform(post("/api/follows/" + targetUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 2. Doğrulama: Veritabanında takip ilişkisi var mı?
        boolean existsInDb = followRepository.findByFollowerIdAndFollowingId(
                userRepository.findByUsername(FOLLOWER_USERNAME).get().getId(),
                targetUser.getId()).isPresent();

        assertTrue(existsInDb, "Takip ilişkisi veritabanına kaydedilmedi!");

        // 3. Doğrulama: Redis Counter arttı mı?
        Integer followerCount = (Integer) redisTemplate.opsForValue().get("user:followers:count:" + targetUser.getId());

        assertNotNull(followerCount);
        assertEquals(1, followerCount, "Redis takipçi sayısı güncellenmedi!");
    }

    @Test
    @DisplayName("Kendi Kendini Takip Etme Hatası")
    @WithMockUser(username = "kutukalan")
    void followUser_SelfFollow_ShouldFail() throws Exception {

        mockMvc.perform(post("/api/follows/" + targetUser.getId()))
                .andExpect(status().isBadRequest());
    }

}
