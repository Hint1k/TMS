package com.demo.tms.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
    }

    @Test
    void cacheManager_ShouldBeConfiguredCorrectly() {
        CacheManager cacheManager = cacheConfig.cacheManager();

        assertNotNull(cacheManager, "CacheManager should not be null");
        assertInstanceOf(CaffeineCacheManager.class, cacheManager,
                "CacheManager should be an instance of CaffeineCacheManager");

        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;

        assertTrue(caffeineCacheManager.getCacheNames().contains("tasks"),
                "CacheManager should contain 'tasks' cache");
        assertTrue(caffeineCacheManager.getCacheNames().contains("comments"),
                "CacheManager should contain 'comments' cache");
    }
}