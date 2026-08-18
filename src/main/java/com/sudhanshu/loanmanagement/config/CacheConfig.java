package com.sudhanshu.loanmanagement.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * In-process caching with Caffeine.
 * Suitable for single-instance deployments.
 * For multi-instance, switch to Redis cache manager.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String LOAN_PRODUCTS = "loanProducts";
    public static final String USER_DETAILS = "userDetails";
    public static final String DASHBOARD_STATS = "dashboardStats";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                LOAN_PRODUCTS, USER_DETAILS, DASHBOARD_STATS);

        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());

        return manager;
    }
}
