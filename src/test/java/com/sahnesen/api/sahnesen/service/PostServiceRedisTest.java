package com.sahnesen.api.sahnesen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.services.PostService;
import com.sahnesen.api.sahnesen.util.SlugUtil;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Session'un test sonuna kadar açık kalmasını sağlar, böylece lazy loading
               // çalışır
public class PostServiceRedisTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String TRENDING_KEY = "posts:trending";

    private String dynamicSlug; // Test sırasında oluşturulan postun slug'ını saklamak için

    @BeforeEach
    void setUp() {

        redisTemplate.delete(TRENDING_KEY); // Test öncesi Redis'teki ilgili key'i temizle
        postRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .slug("kutukalanumut")
                .name("Umut")
                .surname("Kütükalan")
                .username("kutukalanumut")
                .email("kutukalan@gmail.com")
                .password("p4ssword")
                .build();
        userRepository.save(user);

        Post post = Post.builder()
                .postType(PostType.BLOG)
                .title("Redis Test Posts")
                .slug(SlugUtil.generateSlug("Redis Test Posts"))
                .content("Test Content")
                .coverImage(null)
                .user(user)
                .isPublished(true)
                .build();

        Post savedPost = postRepository.save(post);
        this.dynamicSlug = savedPost.getSlug(); // Kaydedilen postun slug'ını alıyoruz
    }

    @Test
    void shouldIncrementViewCountingRedisCorrectly() {
        // İlk İzlenme
        postService.getPostWithViewCount(dynamicSlug);

        Double score = redisTemplate.opsForZSet().score(TRENDING_KEY, dynamicSlug);
        assertEquals(1.0, score, "İlk izlemede skor 1 olmalı");

        postService.getPostWithViewCount(dynamicSlug);
        score = redisTemplate.opsForZSet().score(TRENDING_KEY, dynamicSlug);
        assertEquals(2.0, score, "İkinci izlemede skor 2 olmalı");
    }

}
