package com.sahnesen.api.sahnesen.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.CreateCollectionRequest;
import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.BookmarkCollectionRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkCollectionService {

    private final BookmarkCollectionRepository collectionRepository;
    private final UserRepository userRepository;

    // Yardımcı metot: Username üzerinden User nesnesini bulur
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
    }

    @Transactional(readOnly = true)
    public List<BookmarkCollection> getUserCollections(String username) {
        User user = getUserByUsername(username);
        return collectionRepository.findByUserId(user.getId());
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
}