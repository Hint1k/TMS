package com.demo.tms.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * The {@code CacheConfig} class configures caching for the application using Caffeine.
 * It enables caching and defines a {@link CacheManager} bean that manages the cache for tasks and comments.
 * The cache is configured to expire entries 10 minutes after they are written and have a maximum size of 100 entries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates and configures a {@link CacheManager} bean.
     * <p>
     * This method sets up a {@link CaffeineCacheManager} with two caches: "tasks" and "comments".
     * The cache is configured to expire entries after 10 minutes and to hold a maximum of 100 entries.
     * </p>
     *
     * @return A {@link CacheManager} instance configured with Caffeine settings.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("tasks", "comments");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(100));
        return cacheManager;
    }
}