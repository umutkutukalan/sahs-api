package com.sahnesen.api.sahnesen.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.CreateCollectionRequest;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.PostBookmarkRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkCollectionService {

    private final BookmarkCollectionRepository collectionRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    // Yardımcı metot: Username üzerinden User nesnesini bulur
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }

    @Transactional(readOnly = true)
    public List<BookmarkCollection> getUserCollections(String username) {
        // userId yerine direkt username tabanlı repository metodu kullanılıyor
        return collectionRepository.findByUser_Username(username);
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
    public Page<PostSummaryResponse> getBookmarkedPosts(String username, Pageable pageable) {
        // Doğru repository olan PostBookmarkRepository üzerinden username ile sayfalı
        // çekiyoruz
        Page<PostBookmark> bookmarks = bookmarkRepository.findByCollection_User_Username(username, pageable);

        return bookmarks.map(bookmark -> convertToSummaryResponse(bookmark.getPost()));
    }

    private PostSummaryResponse convertToSummaryResponse(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSubtitle(),
                post.getSlug(),
                post.getCoverImage(),
                post.getPostType(),
                post.getCreatedAt(),
                post.getViewCount(),
                post.getUser().getName(),
                post.getUser().getSurname(),
                post.getUser().getUsername(),
                post.getUser().getProfileImg());
    }
}