package com.sahnesen.api.sahnesen.services;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.PostInteractionStatusDTO;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.dto.PublicUserDTO;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.entities.PostReaction;
import com.sahnesen.api.sahnesen.entities.Tag;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.ReactionType;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.PostBookmarkRepository;
import com.sahnesen.api.sahnesen.repository.PostReactionRepository;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostInteractionService {

        private final PostReactionRepository reactionRepository;
        private final PostBookmarkRepository bookmarkRepository;
        private final BookmarkCollectionRepository collectionRepository;
        private final PostRepository postRepository;
        private final UserRepository userRepository;

        private User getUserByUsername(String usernameOrEmail) {
                return userRepository.findByUsername(usernameOrEmail)
                                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Kullanıcı bulunamadı: " + usernameOrEmail)));
        }

        @Transactional
        public boolean toggleReaction(String username, Long postId, ReactionType reactionType) {
                User user = getUserByUsername(username);
                var existing = reactionRepository.findByUser_UsernameAndPostIdAndReactionType(username, postId,
                                reactionType);

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

                BookmarkCollection collection;
                if (collectionId != null) {
                        collection = collectionRepository.findById(collectionId)
                                        .orElseThrow(() -> new RuntimeException("Klasör bulunamadı"));
                } else {
                        collection = collectionRepository.findByUser_UsernameAndIsDefaultTrue(username)
                                        .orElseGet(() -> collectionRepository.save(
                                                        BookmarkCollection.builder()
                                                                        .name("Kaydedilenler")
                                                                        .isDefault(true)
                                                                        .user(user)
                                                                        .build()));
                }

                var existing = bookmarkRepository.findByCollection_User_UsernameAndPostId(username, postId);
                if (existing.isPresent()) {
                        bookmarkRepository.delete(existing.get());
                        return false;
                } else {
                        Post post = postRepository.findById(postId)
                                        .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

                        PostBookmark bookmark = PostBookmark.builder()
                                        .collection(collection)
                                        .post(post)
                                        .build();
                        bookmarkRepository.save(bookmark);
                        return true;
                }
        }

        @Transactional(readOnly = true)
        public PostInteractionStatusDTO getInteractionStatus(String username, Long postId,
                        ReactionType targetShineType) {
                // Kullanıcının varlığını doğrula
                getUserByUsername(username);

                boolean isLiked = reactionRepository.existsByUser_UsernameAndPostIdAndReactionType(
                                username, postId, ReactionType.LIKE);

                // O spesifik mod parlatma türüne ait kontrol (örn: SHINE_YANYANA)
                boolean isShined = reactionRepository.existsByUser_UsernameAndPostIdAndReactionType(
                                username, postId, targetShineType);

                boolean isBookmarked = bookmarkRepository.existsByCollection_User_UsernameAndPostId(username, postId);

                long likeCount = reactionRepository.countByPostIdAndReactionType(postId, ReactionType.LIKE);
                long shineCount = reactionRepository.countByPostIdAndReactionType(postId, targetShineType);

                return PostInteractionStatusDTO.builder()
                                .isLiked(isLiked)
                                .isShined(isShined)
                                .isBookmarked(isBookmarked)
                                .likeCount(likeCount)
                                .shineCount(shineCount)
                                .build();
        }

        @Transactional(readOnly = true)
        public Page<PostSummaryResponse> getLikedPosts(String username, Pageable pageable) {
                // Kullanıcının varlığını doğrula
                getUserByUsername(username);

                // Kullanıcının LIKE türündeki reaksiyonlarını sayfalı olarak çek
                Page<PostReaction> reactions = reactionRepository.findByUser_UsernameAndReactionType(
                                username, ReactionType.LIKE, pageable);

                // Post nesnelerini PostSummaryResponse record'una map et
                return reactions.map(reaction -> {
                        Post post = reaction.getPost();
                        var author = post.getUser();

                        List<String> tagNames = post.getTags() != null
                                        ? post.getTags().stream().map(Tag::getName).toList()
                                        : Collections.emptyList();

                        return new PostSummaryResponse(
                                        post.getId(),
                                        post.getTitle(),
                                        post.getSubtitle(),
                                        post.getSlug(),
                                        post.getCoverImage(),
                                        post.getPostType(),
                                        tagNames,
                                        post.getCreatedAt(),
                                        post.getViewCount(),
                                        author != null ? author.getName() : null,
                                        author != null ? author.getSurname() : null,
                                        author != null ? author.getUsername() : null,
                                        author != null ? author.getProfileImg() : null);
                });
        }
}