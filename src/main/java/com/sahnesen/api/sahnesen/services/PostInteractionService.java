package com.sahnesen.api.sahnesen.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.PostInteractionStatusDTO;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.entities.PostReaction;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.ReactionType;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.PostBookmarkRepository;
import com.sahnesen.api.sahnesen.repository.PostReactionRepository;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository; // Kullanıcıyı bulmak için gerekli

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostReactionRepository reactionRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final BookmarkCollectionRepository collectionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository; // Eklendi

    private User getUserByUsername(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + usernameOrEmail)));
    }

    @Transactional
    public boolean toggleReaction(String username, Long postId, ReactionType reactionType) {
        User user = getUserByUsername(username);
        var existing = reactionRepository.findByUserIdAndPostIdAndReactionType(user.getId(), postId, reactionType);

        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            return false; // Kaldırıldı
        } else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

            PostReaction reaction = PostReaction.builder()
                    .user(user)
                    .post(post)
                    .reactionType(reactionType)
                    .build();
            reactionRepository.save(reaction);
            return true; // Eklendi
        }
    }

    @Transactional
    public boolean toggleBookmark(String username, Long postId, Long collectionId) {
        User user = getUserByUsername(username);

        // Kullanıcı klasör ID göndermediyse varsayılan klasörünü bul/oluştur
        BookmarkCollection collection;
        if (collectionId != null) {
            collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Klasör bulunamadı"));
        } else {
            collection = collectionRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .orElseGet(() -> collectionRepository.save(
                            BookmarkCollection.builder()
                                    .name("Kaydedilenler")
                                    .isDefault(true)
                                    .user(user)
                                    .build()));
        }

        var existing = bookmarkRepository.findByCollectionUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false; // Kayıtlardan çıkarıldı
        } else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

            PostBookmark bookmark = PostBookmark.builder()
                    .collection(collection)
                    .post(post)
                    .build();
            bookmarkRepository.save(bookmark);
            return true; // Kaydedildi
        }
    }

    @Transactional(readOnly = true)
    public PostInteractionStatusDTO getInteractionStatus(String username, Long postId) {
        User user = getUserByUsername(username);
        Long userId = user.getId();

        boolean isLiked = reactionRepository.existsByUserIdAndPostIdAndReactionType(userId, postId, ReactionType.LIKE);
        boolean isShined = reactionRepository.existsByUserIdAndPostIdAndReactionType(userId, postId,
                ReactionType.SHINE);
        boolean isBookmarked = bookmarkRepository.existsByCollectionUserIdAndPostId(userId, postId);

        long likeCount = reactionRepository.countByPostIdAndReactionType(postId, ReactionType.LIKE);
        long shineCount = reactionRepository.countByPostIdAndReactionType(postId, ReactionType.SHINE);

        // BURAYA LOG EKLEYELİM:
        System.out.println("--- GET INTERACTION STATUS ---");
        System.out.println("Kullanıcı ID: " + userId + " | Username: " + username);
        System.out.println("Post ID: " + postId);
        System.out.println("isLiked Veritabanı Sonucu: " + isLiked);
        System.out.println("Like Sayısı: " + likeCount);

        return PostInteractionStatusDTO.builder()
                .isLiked(isLiked)
                .isShined(isShined)
                .isBookmarked(isBookmarked)
                .likeCount(likeCount)
                .shineCount(shineCount)
                .build();
    }
}