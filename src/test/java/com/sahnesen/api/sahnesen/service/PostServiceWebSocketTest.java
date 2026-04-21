package com.sahnesen.api.sahnesen.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;
import com.sahnesen.api.sahnesen.services.PostService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PostServiceWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private WebSocketStompClient stompClient;
    private BlockingQueue<Long> blockingQueue;
    private String dynamicSlug;
    private static final String TRENDING_KEY = "posts:trending";

    @BeforeEach
    void setUp() {
        // 1. Temizlik
        redisTemplate.delete(TRENDING_KEY);
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 2. Veri Hazırlığı (Senin yapın)
        User user = User.builder()
                .slug("kutukalanumut")
                .name("Umut")
                .surname("Kütükalan")
                .username("kutukalanumut")
                .email("kutukalan@gmail.com")
                .password("p4ssword")
                .build();
        userRepository.save(user);

        Post post = Post.builder()
                .postType(PostType.BLOG)
                .title("Redis Test Posts")
                .slug("redis-test-posts") // Sabit veya dynamicSlug kullanabilirsin
                .content("Test Content")
                .user(user)
                .isPublished(true)
                .build();

        Post savedPost = postRepository.save(post);
        this.dynamicSlug = savedPost.getSlug();

        // 3. WebSocket Client Hazırlığı
        blockingQueue = new LinkedBlockingQueue<>();
        StandardWebSocketClient rawClient = new StandardWebSocketClient();
        List<Transport> transports = List.of(new WebSocketTransport(rawClient));
        this.stompClient = new WebSocketStompClient(new SockJsClient(transports));
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Transactional
    @Test
    void shouldSendWebSocketMessageWhenViewCountIncrements() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws-sahnesen";

        // WebSocket'e bağlan
        StompSession session = stompClient
                .connectAsync(wsUrl, new StompSessionHandlerAdapter() {
                })
                .get(5, TimeUnit.SECONDS);

        // Dinlemeye başla
        session.subscribe("/topic/post-views/" + dynamicSlug, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Long.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((Long) payload);
            }
        });

        // Servisi tetikle
        postService.getPostWithViewCount(dynamicSlug);

        // Doğrula
        Long receivedValue = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedValue, "WebSocket üzerinden mesaj gelmedi!");
        assertEquals(1L, receivedValue);

        System.out.println("Başarılı! WebSocket üzerinden gelen veri: " + receivedValue);
    }
}