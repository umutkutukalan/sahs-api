package com.sahnesen.api.sahnesen.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import com.sahnesen.api.sahnesen.enums.NotificationType;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.enums.ReactionType;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.PostBookmarkRepository;
import com.sahnesen.api.sahnesen.repository.PostReactionRepository;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostInteractionService {

        private final PostReactionRepository reactionRepository;
        private final PostBookmarkRepository bookmarkRepository;
        private final BookmarkCollectionRepository collectionRepository;
        private final PostRepository postRepository;
        private final UserRepository userRepository;
        private final NotificationService notificationService;

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

                        // Eğer beğenilen tür LIKE ise ve postun sahibi beğenen kişi değilse bildirim at
                        if (reactionType == ReactionType.LIKE && post.getUser() != null
                                        && !post.getUser().getId().equals(user.getId())) {
                                try {
                                        notificationService.createNotification(
                                                        post.getUser().getId(),
                                                        "Yeni Beğeni",
                                                        user.getName() + " " + user.getSurname() + " \""
                                                                        + post.getTitle()
                                                                        + "\" adlı içeriğini beğendi.",
                                                        NotificationType.POST_LIKE,
                                                        "/" + post.getUser().getUsername() + "/" + post.getSlug());
                                } catch (Exception e) {
                                        log.error("Beğeni bildirimi gönderilemedi: ", e);
                                }
                        }

                        return true; // Eklendi
                }
        }

        @Transactional
        public void toggleBookmark(String username, Long postId, Long collectionId) {
                Post post = postRepository.findById(postId)
                                .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

                if (collectionId != null) {
                        // Belirli bir koleksiyona kaydetme/kaldırma (modal üzerinden gelen çağrı)
                        BookmarkCollection collection = collectionRepository.findById(collectionId)
                                        .orElseThrow(() -> new RuntimeException("Koleksiyon bulunamadı"));

                        if (!collection.getUser().getUsername().equals(username)) {
                                throw new RuntimeException("Yetkiniz yok.");
                        }

                        Optional<PostBookmark> existingBookmark = bookmarkRepository
                                        .findByCollectionIdAndPostId(collection.getId(), postId);

                        if (existingBookmark.isPresent()) {
                                bookmarkRepository.delete(existingBookmark.get());
                        } else {
                                PostBookmark bookmark = PostBookmark.builder()
                                                .collection(collection)
                                                .post(post)
                                                .build();
                                bookmarkRepository.save(bookmark);
                        }
                } else {
                        // collectionId gönderilmediyse: "kaydı tamamen kaldır" niyeti
                        // Postun kullanıcı için sahip olduğu TÜM bookmark kayıtlarını sil
                        List<PostBookmark> existingBookmarks = bookmarkRepository
                                        .findAllByCollection_User_UsernameAndPostId(username, postId);

                        if (!existingBookmarks.isEmpty()) {
                                bookmarkRepository.deleteAll(existingBookmarks);
                        } else {
                                // Hiç kayıtlı değilse (edge case, normalde frontend buraya düşmemeli):
                                // güvenlik amaçlı varsayılan koleksiyona ekle
                                BookmarkCollection defaultCollection = collectionRepository
                                                .findByUser_UsernameAndIsDefaultTrue(username)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Varsayılan koleksiyon bulunamadı"));

                                PostBookmark bookmark = PostBookmark.builder()
                                                .collection(defaultCollection)
                                                .post(post)
                                                .build();
                                bookmarkRepository.save(bookmark);
                        }
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
        public Page<PostSummaryResponse> getLikedPosts(String username,
                        PostType postType, Pageable pageable) {
                // Kullanıcının varlığını doğrula
                getUserByUsername(username);

                // Repository üzerinden doğrudan filtrelenmiş Post nesnelerini çek
                Page<Post> posts = reactionRepository.findLikedPostsByUsernameWithFilter(
                                username, ReactionType.LIKE, postType, pageable);

                // Post nesnelerini PostSummaryResponse record'una map et
                return posts.map(post -> {
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
                                        post.getDiscussionEndsAt(),
                                        post.getDiscussionDurationHours(),
                                        author != null ? author.getName() : null,
                                        author != null ? author.getSurname() : null,
                                        author != null ? author.getUsername() : null,
                                        author != null ? author.getProfileImg() : null);
                });
        }
}