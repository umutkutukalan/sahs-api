package com.sahnesen.api.sahnesen.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableCaching
public class CacheConfig {

        // Ortak ObjectMapper'ı bir metoda ayıralım ki hem Cache hem Template kullansın
        private ObjectMapper createObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                                .allowIfSubType("com.sahnesen")
                                                .build(),
                                DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);
                return objectMapper;
        }

        @Bean
        @Primary
        @Profile("!test")
        public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // ✅ Daha güvenli alternatif
                objectMapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                                .allowIfSubType("com.sahnesen") // sadece kendi paketlerin
                                                .build(),
                                DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .disableCachingNullValues()
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer));

                return RedisCacheManager.builder(redisConnectionFactory)
                                .cacheDefaults(config)
                                .build();
        }

        // FollowService'de kullanmak üzere RedisTemplate tanımlamamız gerekiyor
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key'ler düz yazı (String) olsun
                template.setKeySerializer(new StringRedisSerializer());
                // Value'lar bizim güvenli JSON ayarımızla kaydedilsin
                template.setValueSerializer(new GenericJackson2JsonRedisSerializer(createObjectMapper()));

                template.setHashKeySerializer(new StringRedisSerializer());
                template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(createObjectMapper()));

                return template;
        }
}