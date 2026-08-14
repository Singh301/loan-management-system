package com.sudhanshu.loanmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token blacklist: uses Redis when available, otherwise in-memory fallback.
 * Redis is required for multi-instance production safety.
 */
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "loan:blacklist:";

    private final Map<String, Long> localBlacklist = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    public void blacklist(String token, long expiresAtEpochMillis) {
        if (token == null || token.isBlank()) {
            return;
        }
        long ttlMs = Math.max(0, expiresAtEpochMillis - System.currentTimeMillis());
        if (ttlMs == 0) {
            return;
        }

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", Duration.ofMillis(ttlMs));
                return;
            } catch (Exception ignored) {
                // fall through to local
            }
        }
        localBlacklist.put(token, expiresAtEpochMillis);
        cleanupLocal();
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        if (redisTemplate != null) {
            try {
                Boolean has = redisTemplate.hasKey(KEY_PREFIX + token);
                if (Boolean.TRUE.equals(has)) {
                    return true;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        Long expiry = localBlacklist.get(token);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            localBlacklist.remove(token);
            return false;
        }
        return true;
    }

    private void cleanupLocal() {
        long now = System.currentTimeMillis();
        localBlacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
