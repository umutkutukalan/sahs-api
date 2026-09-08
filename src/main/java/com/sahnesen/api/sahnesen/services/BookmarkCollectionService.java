package com.sahnesen.api.sahnesen.services;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.BookmarkCollectionResponse;
import com.sahnesen.api.sahnesen.dto.CreateCollectionRequest;
import com.sahnesen.api.sahnesen.dto.PostPreviewDTO;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.entities.Tag;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.PostBookmarkRepository;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkCollectionService {

    private final BookmarkCollectionRepository collectionRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Yardımcı metot: Username üzerinden User nesnesini bulur
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }

    @Transactional(readOnly = true)
    public List<BookmarkCollectionResponse> getUserCollections(String username) {
        List<BookmarkCollection> collections = collectionRepository.findByUser_Username(username);

        return collections.stream().map(col -> {
            // Son eklenen 4 postu alıp PostPreviewDTO'ya dönüştürüyoruz
            List<PostPreviewDTO> previewContents = col.getBookmarks() != null ? col.getBookmarks().stream()
                    .sorted((b1, b2) -> b2.getId().compareTo(b1.getId())) // En son eklenenler önce gelsin
                    .limit(4)
                    .map(b -> new PostPreviewDTO(
                            b.getPost().getId(),
                            b.getPost().getTitle(),
                            b.getPost().getCoverImage()))
                    .toList()
                    : Collections.emptyList();

            return new BookmarkCollectionResponse(
                    col.getId(),
                    col.getName(),
                    col.getDescription(),
                    col.isDefault(),
                    previewContents);
        }).toList();
    }

    @Transactional
    public BookmarkCollection createCollection(String username, CreateCollectionRequest request) {
        User user = getUserByUsername(username);

        BookmarkCollection collection = BookmarkCollection.builder()
                .name(request.name())
                .description(request.description())
                .user(user)
                .isDefault(false) // Yeni oluşturulanlar varsayılan olmaz
                .build();

        return collectionRepository.save(collection);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getBookmarkedPosts(String username, PostType postType, Pageable pageable) {
        // Doğru repository olan PostBookmarkRepository üzerinden username ile sayfalı
        // çekiyoruz
        Page<PostBookmark> bookmarks = bookmarkRepository.findByCollection_User_Username(username, postType, pageable);

        return bookmarks.map(bookmark -> convertToSummaryResponse(bookmark.getPost()));
    }

    @Transactional
    public void addPostToCollection(String username, Long collectionId, Long postId) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Koleksiyon bulunamadı: " + collectionId));

        // Güvenlik kontrolü: Bu koleksiyon gerçekten bu kullanıcıya mı ait?
        if (!collection.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu koleksiyona içerik ekleme yetkiniz yok.");
        }

        // 💡 Doğru olan: Post'u postId üzerinden buluyoruz
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + postId));

        // Daha önce bu koleksiyona eklenmiş mi kontrol et
        boolean alreadyExists = bookmarkRepository.existsByCollectionIdAndPostId(collectionId, postId);
        if (!alreadyExists) {
            PostBookmark bookmark = PostBookmark.builder()
                    .collection(collection)
                    .post(post)
                    .build();
            bookmarkRepository.save(bookmark);
        }
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPostsByCollectionId(String username, Long collectionId, Pageable pageable) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Koleksiyon bulunamadı: " + collectionId));

        // Güvenlik kontrolü
        if (!collection.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu koleksiyona erişim yetkiniz yok.");
        }

        Page<PostBookmark> bookmarks = bookmarkRepository.findByCollectionId(collectionId, pageable);
        return bookmarks.map(bookmark -> convertToSummaryResponse(bookmark.getPost()));
    }

    @Transactional
    public BookmarkCollection updateCollection(String username, Long collectionId, CreateCollectionRequest request) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Koleksiyon bulunamadı: " + collectionId));

        // Güvenlik kontrolü
        if (!collection.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu koleksiyonu düzenleme yetkiniz yok.");
        }

        // Varsayılan koleksiyonların ismi değiştirilmek istenmeyebilir (İsteğe bağlı
        // kural)
        if (collection.isDefault()) {
            throw new RuntimeException("Varsayılan koleksiyonlar düzenlenemez.");
        }

        collection.setName(request.name());
        collection.setDescription(request.description());

        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(String username, Long collectionId) {
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Koleksiyon bulunamadı: " + collectionId));

        // Güvenlik kontrolü
        if (!collection.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu koleksiyonu silme yetkiniz yok.");
        }

        if (collection.isDefault()) {
            throw new RuntimeException("Varsayılan koleksiyonlar silinemez.");
        }

        collectionRepository.delete(collection);
    }

    private PostSummaryResponse convertToSummaryResponse(Post post) {

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
                post.getUser().getName(),
                post.getUser().getSurname(),
                post.getUser().getUsername(),
                post.getUser().getProfileImg());
    }
}