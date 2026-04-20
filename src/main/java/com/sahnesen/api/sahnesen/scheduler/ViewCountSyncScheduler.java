package com.sahnesen.api.sahnesen.scheduler;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountSyncScheduler {

    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String TRENDING_KEY = "posts:trending";

    @Scheduled(fixedRate = 900000) // Her 15 dakikada bir çalışır
    @Transactional
    public void syncViewCountsToDb() {
        log.info("Redis -> DB izlenme sayıları senkronizasyonu başladı...");

        // 1. Redis'teki tüm skorları çekiyoruz
        Set<ZSetOperations.TypedTuple<String>> results = redisTemplate.opsForZSet().rangeWithScores(TRENDING_KEY, 0,
                -1);
        if (results == null || results.isEmpty()) {
            log.info("Senkronize edilecek izleme verisi bulunamadı.");
            return;
        }

        for (ZSetOperations.TypedTuple<String> result : results) {
            String slug = result.getValue();
            Double score = result.getScore();

            if (slug != null && score != null) {
                // 2. Veritabanına mühürleme işlemini yapıyoruz
                postRepository.findBySlug(slug).ifPresent(post -> {
                    post.setViewCount(score.longValue());
                    postRepository.save(post);
                });
            }
        }
        log.info("Senkronizasyon başarıyla tamamlandı.");
    }

}
