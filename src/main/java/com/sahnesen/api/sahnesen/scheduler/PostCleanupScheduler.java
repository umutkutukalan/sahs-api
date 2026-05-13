package com.sahnesen.api.sahnesen.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.services.FileService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostCleanupScheduler {

    private final PostRepository postRepository;
    private final FileService fileService;

    @Scheduled(cron = "0 0 3 * * SUN") // Her Pazar sabah 3'te çalışır
    @Transactional
    public void cleanupAbandonedDrafts() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(30);

        // 30 gündür güncellenmemiş ve hala yayınlanmamış taslakları bul
        List<Post> abandonedPosts = postRepository.findAllByIsPublishedFalseAndUpdatedAtBefore(limitDate);
        
        abandonedPosts.forEach(post -> {
            // Önce fiziksel klasörü sil
            fileService.deletePostFolder(post.getId());
            // Sonra DB kaydı sil
            postRepository.delete(post);
        

        });
    }
}
