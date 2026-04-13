package com.sahnesen.api.sahnesen.services;

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

}
