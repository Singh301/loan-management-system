package com.sudhanshu.loanmanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Redis is activated when spring.data.redis.host is set.
 * TokenBlacklistService and cache can use it for multi-instance deployments.
 * Local/dev without Redis continues with in-memory blacklist + Caffeine.
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
    // Spring Boot auto-configures RedisConnectionFactory + StringRedisTemplate
}
