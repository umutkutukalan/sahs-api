
package com.sahnesen.api.sahnesen.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TestCacheConfig {

    @Bean
    @Profile("test")
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }

}