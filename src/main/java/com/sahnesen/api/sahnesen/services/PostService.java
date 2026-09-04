package com.sahnesen.api.sahnesen.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.dto.PostSummaryResponse;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.Tag;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.TagRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.response.PostResponse;
import com.sahnesen.api.sahnesen.util.SlugUtil;
import com.sahnesen.api.sahnesen.util.TiptapContentExtractor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    @Autowired
    private HttpServletRequest request; // WebSocket bağlantısında kullanmak üzere ekliyoruz

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TiptapContentExtractor tiptapContentExtractor;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Post oluşturulduğunda bildirim göndermek için ekliyoruz
    private final FileService fileService; // Dosya işlemleri için ekliyoruz

    private final StringRedisTemplate redisTemplate; // Redis işlemleri için ekliyoruz

    private static final String TRENDING_KEY = "posts:trending";

    private final SimpMessagingTemplate messagingTemplate; // WebSocket üzerinden mesaj göndermek için

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PostResponse createPost(String username, PostRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // 💡 MEDIUM TASLAK SLUG MANTIĞI:
        // Eğer direkt yayınlanarak oluşturuluyorsa (isPublished = true) başlığa göre
        // slug üret.
        // Taslak olarak oluşturuluyorsa (isPublished = false) rastgele benzersiz bir
        // hash ata.
        String slug;
        if (request.isPublished()) {
            slug = generateUniqueSlugForPost(request.getTitle(), null);
        } else {
            slug = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }

        String jsonContentString;
        try {
            jsonContentString = objectMapper.writeValueAsString(request.getContent());
        } catch (Exception e) {
            throw new RuntimeException("İçerik JSON formatına dönüştürülemedi: " + e.getMessage());
        }

        // 1. Cover Image belirleme (DTO'da yoksa Tiptap JSON'dan çıkar)
        String finalCoverImage = request.getCoverImage();
        if (finalCoverImage == null || finalCoverImage.isBlank()) {
            finalCoverImage = tiptapContentExtractor.extractFirstImage(jsonContentString);
        }

        // 2. Subtitle belirleme (DTO'da yoksa Tiptap JSON'dan çıkar)
        String finalSubtitle = request.getSubtitle();
        if (finalSubtitle == null || finalSubtitle.isBlank()) {
            finalSubtitle = tiptapContentExtractor.extractSubtitle(jsonContentString);
        }

        // Süre hesaplama mantığı (Eğer DTO'dan gelmezse varsayılan 3 saat alınır)
        int durationHours = (request.getDiscussionDurationHours() != null && request.getDiscussionDurationHours() > 0)
                ? request.getDiscussionDurationHours()
                : 3;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endsAt = now.plusHours(durationHours);

        // 3. Post nesnesini inşa et
        Post post = Post.builder()
                .postType(request.getPostType())
                .title(request.getTitle())
                .subtitle(finalSubtitle)
                .slug(slug)
                .content(jsonContentString)
                .coverImage(finalCoverImage)
                .user(user)
                .tags(processAndGetTags(request.getTags()))
                .isPublished(request.isPublished())
                .discussionDurationHours(durationHours)
                .discussionEndsAt(endsAt)
                .build();

        Post savedPost = postRepository.save(post);

        fileService.createPostFolder(savedPost.getId());

        if (savedPost.isPublished()) {
            notificationService.notifyFollowers(
                    user.getId(),
                    user.getUsername(),
                    savedPost.getTitle(),
                    savedPost.getSlug());
        }

        return convertToResponse(savedPost);
    }

    private Set<Tag> processAndGetTags(List<String> rawTagNames) {
        Set<Tag> tags = new HashSet<>();
        if (rawTagNames == null || rawTagNames.isEmpty())
            return tags;

        for (String rawName : rawTagNames) {
            String cleanName = rawName.trim().toLowerCase();
            if (cleanName.isEmpty())
                continue;

            // Veritabanında varsa al, yoksa yeni oluştur
            Tag tag = tagRepository.findByNameIgnoreCase(cleanName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(cleanName).build()));

            tags.add(tag);
        }
        return tags;
    }

    public void validatePostOwnership(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu yazıyı düzenleme yetkiniz yok");
        }
    }

    // Sadece giriş yapan kullanıcının kendi (taslaklar dahil) tüm postlarını
    // görmesi için
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyOwnPosts(String username, Boolean isPublished, PostType postType,
            Pageable pageable) {
        return postRepository.findMyOwnPostsWithFilter(username, isPublished, postType, pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    @CacheEvict(value = "postBySlug", key = "#result.slug", condition = "#result != null && #result.isPublished()")
    public PostResponse updatePost(String username, Long postId, PostRequestDTO request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu yazıyı düzenleme yetkiniz yok");
        }

        // 💡 MEDIUM SLUG MANTIĞI:
        // Eğer yayınlama isteği geldiyse (isPublished = true) başlığa göre gerçek slug
        // üretilir.
        if (request.isPublished()) {
            // Yazı daha önce yayınlanmadıysa VEYA yayınlanmış ama başlığı değiştiyse:
            if (!post.isPublished() || !post.getTitle().equals(request.getTitle())) {
                String newSlug = generateUniqueSlugForPost(request.getTitle(), post.getId());
                post.setSlug(newSlug);
            }
        }
        // NOT: isPublished false ise (Auto-save aşaması) post.setSlug() HİÇ BİR ŞEKİLDE
        // ÇAĞRILMAZ!
        // İlk atanan random hash (örn: 5f661044e2d6) olduğu gibi korunur.

        // Content Map'i String JSON'a dönüştür
        String jsonContentString;
        try {
            jsonContentString = objectMapper.writeValueAsString(request.getContent());
        } catch (Exception e) {
            throw new RuntimeException("İçerik JSON dönüştürme hatası");
        }

        // Cover Image & Subtitle belirleme
        String finalCoverImage = request.getCoverImage();
        if (finalCoverImage == null || finalCoverImage.isBlank()) {
            finalCoverImage = tiptapContentExtractor.extractFirstImage(jsonContentString);
        }

        String finalSubtitle = request.getSubtitle();
        if (finalSubtitle == null || finalSubtitle.isBlank()) {
            finalSubtitle = tiptapContentExtractor.extractSubtitle(jsonContentString);
        }

        // Süre güncellemesi
        int durationHours = (request.getDiscussionDurationHours() != null && request.getDiscussionDurationHours() > 0)
                ? request.getDiscussionDurationHours()
                : (post.getDiscussionDurationHours() != null ? post.getDiscussionDurationHours() : 3);

        // Eğer yazı yeni yayınlanıyorsa veya süre değiştiyse bitiş zamanını yeniden
        // hesapla
        if (request.isPublished() && !post.isPublished()) {
            post.setDiscussionEndsAt(LocalDateTime.now().plusHours(durationHours));
        }

        post.setTitle(request.getTitle());
        post.setSubtitle(finalSubtitle);
        post.setContent(jsonContentString);
        post.setCoverImage(finalCoverImage);
        post.setPostType(request.getPostType());
        post.setPublished(request.isPublished());
        post.setTags(processAndGetTags(request.getTags()));
        post.setDiscussionDurationHours(durationHours);

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }

    // Yardımcı Metod: Başka postlarda var mı kontrol eder
    private String generateUniqueSlugForPost(String title, Long currentPostId) {
        String baseSlug = SlugUtil.generateSlug(title);
        String slug = baseSlug;
        int count = 1;

        while (postRepository.existsBySlugAndIdNot(slug, currentPostId)) {
            slug = baseSlug + "-" + count;
            count++;
        }

        return slug;
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

    // Genel Akis (Herkes gorebilir - Yalnizca yayinlanmis içerikler)
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getAllPublishedPosts(PostType type, Pageable pageable) {
        return postRepository.findAllPublishedWithFilter(type, pageable)
                .map(this::convertToSummaryResponse);
    }

    // Profil Sayfasi
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getUserPosts(String username, PostType type, Pageable pageable) {
        return postRepository.findByUserUsernameAndPublishedWithFilter(username, type, pageable)
                .map(this::convertToSummaryResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "postBySlug", key = "#slug", condition = "#currentUsername == null", // Sadece anonim/genel okuma
                                                                                            // isteklerini cache'le
            unless = "#result == null || !#result.isPublished" // Taslak olan veya null dönen yanıtları ASLA cache'leme
    )
    public PostResponse getPostBySlug(String slug, String currentUsername) {
        // 1. Önce slug ile post'u bul (isPublished şartı olmadan)
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Yazı bulunamadı."));

        // 2. Güvenlik ve Yayın Kontrolü:
        // Eğer yazı yayınlanmamışsa (TASLAK) VE isteği atan kişi yazının sahibi değilse
        // (veya anonimse) erişimi engelle!
        boolean isOwner = currentUsername != null && currentUsername.equals(post.getUser().getUsername());

        if (!post.isPublished() && !isOwner) {
            throw new RuntimeException("Bu taslağı görüntüleme yetkiniz yok.");
        }

        return convertToResponse(post);
    }

    public PostResponse getPostWithViewCount(String slug, String currentUsername) {

        /**
         * Bu metodun amacı, bir yazıya erişildiğinde o yazının görüntülenme sayısını
         * artırmak ve güncel görüntülenme sayısını döndürmektir. Redis'te her yazı için
         * bir anahtar (key) oluşturacağız ve bu anahtar altında görüntülenme sayısını
         * saklayacağız. Her yazıya erişildiğinde bu sayıyı artıracağız ve güncel sayıyı
         * döndüreceğiz. (ZINCRBY komutu ile) Ayrıca, yazının kendisini de
         * veritabanından çekip döndüreceğiz. Böylece, yazıya erişildiğinde hem yazının
         * içeriği hem de güncel görüntülenme sayısı sağlanmış olacak.
         */

        long currentViewCount;

        if (isEligibleToIncreaseView(slug)) {
            Double newScore = redisTemplate.opsForZSet().incrementScore(TRENDING_KEY, slug, 1);
            currentViewCount = (newScore != null) ? newScore.longValue() : 1L;

            messagingTemplate.convertAndSend("/topics/post-views/" + slug, currentViewCount);
        } else {
            Double currentScore = redisTemplate.opsForZSet().score(TRENDING_KEY, slug);
            currentViewCount = (currentScore != null) ? currentScore.longValue() : 0L;
        }

        // Güncellenmiş getPostBySlug metodunu çağırıyoruz
        PostResponse postResponse = getPostBySlug(slug, currentUsername);
        postResponse.setViewCount(currentViewCount);

        return postResponse;
    }

    public List<String> getTopPosts(int limit) {
        // ZREVRANGE komutu ile TRENDING_KEY altında en yüksek skora sahip ilk 'limit'
        // kadar slug'ı çekiyoruz
        Set<String> range = redisTemplate.opsForZSet().reverseRange(TRENDING_KEY, 0, limit - 1);
        return new ArrayList<>(range != null ? range : Collections.emptyList());
    }

    // 💡 HAFİF DTO DÖNÜŞTÜRÜCÜ (Content Yok!)
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
                post.getUser().getProfileImg()
        // 🚫 post.getContent() BURAYA EKLENMİYOR!
        );
    }

    private PostResponse convertToResponse(Post post) {

        // Tag entity setini List<String>'e dönüştürüyoruz
        List<String> tagNames = post.getTags() != null
                ? post.getTags().stream().map(Tag::getName).toList()
                : Collections.emptyList();

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .subtitle(post.getSubtitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .coverImage(post.getCoverImage())
                .postType(post.getPostType())
                .tags(tagNames)
                .isPublished(post.isPublished())
                .createdAt(post.getCreatedAt())
                .discussionEndsAt(post.getDiscussionEndsAt())
                .discussionDurationHours(post.getDiscussionDurationHours())
                .authorName(post.getUser().getName())
                .authorSurname(post.getUser().getSurname())
                .authorUsername(post.getUser().getUsername())
                .authorProfileImg(post.getUser().getProfileImg())
                .build();
    }

    // ----

    private boolean isEligibleToIncreaseView(String slug) {
        // 1. IP Adresini Al (Proxy arkasındaysa X-Forwarded-For'a bak)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        // 2. Redis için benzersiz bir anahtar üret
        String lockKey = "view_lock:" + slug + ":" + ip;

        // 3. Bu anahtar Redis'te var mı? (setIfAbsent atomik bir işlemdir)
        // Eğer yoksa "1" değerini set eder ve 10 dakika TTL koyar, true döner.
        // Eğer varsa hiçbir şey yapamaz ve false döner.
        Boolean isNewVisit = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(isNewVisit);

    }

    // Takip Edilenlerin Akışı
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getFollowingPosts(String username, PostType type, Pageable pageable) {
        return postRepository.findFollowingPostsWithFilter(username, type, pageable)
                .map(this::convertToSummaryResponse);
    }

    // 4 Katmanlı Ağırlıklı Arama Servis Metodu
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> searchPosts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }
        return postRepository.searchPostsWeighted(keyword.trim(), pageable)
                .map(this::convertToSummaryResponse);
    }

    // Auto-complete için etiket arama metodu
    @Transactional(readOnly = true)
    public List<Tag> searchTagsForAutocomplete(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return tagRepository.searchTags(query.trim());
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPostsByTag(String tagName, Pageable pageable) {
        return postRepository.findByTagNameAndPublished(tagName, pageable)
                .map(this::convertToSummaryResponse);
    }

}
