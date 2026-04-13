package com.sahnesen.api.sahnesen.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.util.SlugUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Post createPost(String username, PostRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String slug = SlugUtil.generateSlug(request.getTitle());
        if (postRepository.existsBySlug(slug)) {
            // Slug zaten varsa, benzersiz hale getirmek için zaman damgası ekleyebiliriz
            slug = slug + "-" + System.currentTimeMillis() % 1000;
        }

        // 3. Post nesnesini inşa et
        Post post = Post.builder()
                .postType(request.getPostType())
                .title(request.getTitle())
                .slug(slug)
                .content(request.getContent())
                .coverImage(request.getCoverImage())
                .user(user)
                .isPublished(request.isPublished())
                .build();

        return postRepository.save(post);
    }

    // Sadece giriş yapan kullanıcının kendi (taslaklar dahil) tüm postlarını görmesi için
    public Page<Post> getMyOwnPosts(String username, Pageable pageable) {
        return postRepository.findAllByUser_UsernameOrderByCreatedAtDesc(username, pageable);
    }

    // ----

    public Page<Post> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findAllByIsPublishedTrueOrderByCreatedAtDesc(pageable);
    }

    public Page<Post> getUserPosts(String username, Pageable pageable) {
        return postRepository.findAllByUser_UsernameAndIsPublishedTrueOrderByCreatedAtDesc(username, pageable);
    }

}
