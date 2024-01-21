package com.aichatapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineConfig {

    @Bean
    public CaffeineCache openAIRequestCache() {
        return new CaffeineCache("open_ai_request_cache",
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .build());
    }

    @Bean
    public CaffeineCache openAIRetryRequestCache() {
        return new CaffeineCache("open_ai_retry_request_cache",
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .build());
    }

    @Bean
    public CaffeineCache googleGeminiRequestCache() {
        return new CaffeineCache("google_gemini_request_cache",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());
    }


}
