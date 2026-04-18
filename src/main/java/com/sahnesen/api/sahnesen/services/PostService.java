package com.sahnesen.api.sahnesen.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.response.PostResponse;
import com.sahnesen.api.sahnesen.util.SlugUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate; // Redis işlemleri için ekliyoruz

    public PostResponse createPost(String username, PostRequestDTO request) {
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

        return convertToResponse(postRepository.save(post));
    }

    // Sadece giriş yapan kullanıcının kendi (taslaklar dahil) tüm postlarını
    // görmesi için
    public Page<PostResponse> getMyOwnPosts(String username, Pageable pageable) {
        return postRepository.findAllByUser_UsernameOrderByCreatedAtDesc(username, pageable)
                .map(this::convertToResponse);
    }

    @CacheEvict(value = "postBySlug", key = "#result.slug") // Güncellenen postun slug'ını cache'den sil
    public PostResponse updatePost(String username, Long postId, PostRequestDTO request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu yazıyı düzenleme yetkiniz yok");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCoverImage(request.getCoverImage());
        post.setPostType(request.getPostType());
        post.setPublished(request.isPublished());

        return convertToResponse(postRepository.save(post));
    }

    public void deletePost(String username, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Silinmek istenen post bulunamadı"));
        postRepository.delete(post);

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu postu silme yetkiniz yok");
        }

        postRepository.delete(post);
    }

    // ----

    // Genel Akış (Herkes görebilir)
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findAllByIsPublishedTrueOrderByCreatedAtDesc(pageable).map(this::convertToResponse);
    }

    // Profil sayfası için, sadece o kullanıcıya ait ve yayınlanmış postları getir
    public Page<PostResponse> getUserPosts(String username, Pageable pageable) {
        return postRepository.findAllByUser_UsernameAndIsPublishedTrueOrderByCreatedAtDesc(username, pageable)
                .map(this::convertToResponse);
    }

    @Cacheable(value = "postBySlug", key = "#slug")
    public PostResponse getPostBySlug(String slug) {
        // Bu metodun içine sadece ilk seferde girecek
        // Sonraki isteklerde Spring, Redis'e bakıp sonucu oradan dönecek

        System.out.println("Veritabanına gidiliyor: " + slug); // Cache mekanizmasının çalıştığını görmek için log
                                                               // ekleyelim

        Post post = postRepository.findBySlugAndIsPublishedTrue(slug)
                .orElseThrow(() -> new RuntimeException("Yazı bulunamadı veya henüz yayınlanmadı."));
        return convertToResponse(post);
    }

    private PostResponse convertToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .coverImage(post.getCoverImage())
                .postType(post.getPostType())
                .createdAt(post.getCreatedAt())
                .authorName(post.getUser().getName())
                .authorSurname(post.getUser().getSurname())
                .authorUsername(post.getUser().getUsername())
                .authorProfileImg(post.getUser().getProfileImg())
                .build();
    }

}
