package com.sahnesen.api.sahnesen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        redisTemplate.delete(TRENDING_KEY); // Test sonrası Redis'teki ilgili key'i temizle
    }

    @Test
    @Transactional
    void shouldIncrementViewCountingRedisCorrectly() {
        // İlk İzlenme
        postService.getPostWithViewCount(dynamicSlug);

        Double score = redisTemplate.opsForZSet().score(TRENDING_KEY, dynamicSlug);
        assertEquals(1.0, score, "İlk izlemede skor 1 olmalı");

        postService.getPostWithViewCount(dynamicSlug);
        score = redisTemplate.opsForZSet().score(TRENDING_KEY, dynamicSlug);
        assertEquals(2.0, score, "İkinci izlemede skor 2 olmalı");
    }

    @Test
    void shouldHandleConcurrentViewIncrements() throws InterruptedException {
        int numberOfThreads = 500; // 50 eşzamanlı istek
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    postService.getPostWithViewCount(dynamicSlug);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Double finalScore = redisTemplate.opsForZSet().score(TRENDING_KEY, dynamicSlug);
        assertEquals(500.0, finalScore, "50 eşzamanlı izlenmeden sonra skor 500 olmalı");

        service.shutdown();

    }
}
