package com.sahnesen.api.sahnesen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "uploads" klasörünü "/uploads/**" URL'siyle eşleştiriyoruz
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        registry.addResourceHandler("/profileImgs/**")
                .addResourceLocations("file:./uploads/profileImgs/");
        registry.addResourceHandler("/coverImgs/**")
                .addResourceLocations("file:./uploads/coverImgs/");
    }

}
