package com.sahnesen.api.sahnesen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket mesaj yönetimini aktif ediyoruz
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Sunucudan istemciye gidecek mesajların "/topic" ile başlamasını sağlıyoruz
        config.enableSimpleBroker("/topic");
        // İstemciden sunucuya gidecek mesajların "/app" ile başlamasını sağlıyoruz
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend'in bağlanacağı WebSocket endpoint'ini tanımlıyoruz
        registry.addEndpoint("/ws-sahnesen")
                .setAllowedOriginPatterns("*") // CORS ayarları, tüm originlere izin veriyoruz
                .withSockJS(); // SockJS desteği ekliyoruz, WebSocket desteklenmeyen tarayıcılar için fallback
                               // mekanizması sağlar
    }

}
