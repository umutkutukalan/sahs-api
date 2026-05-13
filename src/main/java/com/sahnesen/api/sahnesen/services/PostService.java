package com.sahnesen.api.sahnesen.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

import com.sahnesen.api.sahnesen.dto.PostRequestDTO;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.response.PostResponse;
import com.sahnesen.api.sahnesen.util.SlugUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    @Autowired
    private HttpServletRequest request; // WebSocket bağlantısında kullanmak üzere ekliyoruz

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Post oluşturulduğunda bildirim göndermek için ekliyoruz
    private final FileService fileService; // Dosya işlemleri için ekliyoruz

    private final StringRedisTemplate redisTemplate; // Redis işlemleri için ekliyoruz

    private static final String TRENDING_KEY = "posts:trending";

    private final SimpMessagingTemplate messagingTemplate; // WebSocket üzerinden mesaj göndermek için

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
                .content(request.getContent().toString())
                .coverImage(request.getCoverImage())
                .user(user)
                .isPublished(request.isPublished())
                .build();

        Post savedPost = postRepository.save(post);

        // ID artık elimizde, klasörü fiziksel olarak oluşturabiliriz
        fileService.createPostFolder(savedPost.getId());

        if (savedPost.isPublished()) {
            notificationService.notifyFollowers(user.getId(), user.getUsername(), savedPost.getTitle(), savedPost.getSlug());
        }

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
        post.setContent(request.getContent().toString());
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

    @Transactional(readOnly = true)
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

    public PostResponse getPostWithViewCount(String slug) {
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

        // 1. Rate Limit Kontrolü: Eğer bu IP son 10 dk içinde bu yazıya bakmadıysa
        if (isEligibleToIncreaseView(slug)) {
            // Skoru 1 artır ve yeni skoru al
            Double newScore = redisTemplate.opsForZSet().incrementScore(TRENDING_KEY, slug, 1);
            currentViewCount = (newScore != null) ? newScore.longValue() : 1L;

            // WebSocket üzerinden TÜM abonelere "Yeni sayı bu!" diye uçur
            messagingTemplate.convertAndSend("/topics/post-views/" + slug, currentViewCount);

            System.out.println("İzlenme arttırıldı. Yeni sayı: " + currentViewCount);
        } else {
            // Eğer IP kilitliyse (Limit takıldıysa), sadece mevcut skoru oku (artırma
            // yapma)
            Double currentScore = redisTemplate.opsForZSet().score(TRENDING_KEY, slug);
            currentViewCount = (currentScore != null) ? currentScore.longValue() : 0L;

            System.out.println("Rate Limit devrede. Mevcut sayı dönülüyor: " + currentViewCount);
        }

        // 2. Post verisini getir (DB veya Cache'den)
        PostResponse postResponse = getPostBySlug(slug);

        // 3. Güncel izlenme sayısını response içine set et
        postResponse.setViewCount(currentViewCount);

        return postResponse;
    }

    public List<String> getTopPosts(int limit) {
        // ZREVRANGE komutu ile TRENDING_KEY altında en yüksek skora sahip ilk 'limit'
        // kadar slug'ı çekiyoruz
        Set<String> range = redisTemplate.opsForZSet().reverseRange(TRENDING_KEY, 0, limit - 1);
        return new ArrayList<>(range != null ? range : Collections.emptyList());
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
}
