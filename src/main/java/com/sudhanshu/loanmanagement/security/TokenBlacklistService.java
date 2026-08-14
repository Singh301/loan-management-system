package com.sudhanshu.loanmanagement.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Revoked access-token store.
 *
 * DEV: in-memory fallback keeps local setup simple.
 * PROD: Redis is mandatory; failures fail closed instead of silently accepting a revoked token
 * on another application instance.
 *
 * The raw JWT is never used as a Redis key. A SHA-256 fingerprint avoids putting bearer-token
 * material into Redis key listings or operational diagnostics.
 */
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "loan:blacklist:";
    private final Map<String, Long> localBlacklist = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final boolean production;

    public TokenBlacklistService(StringRedisTemplate redisTemplate,
                                 @Value("${spring.profiles.active:dev}") String activeProfile) {
        this.redisTemplate = redisTemplate;
        this.production = activeProfile.contains("prod");
    }

    public void blacklist(String token, long expiresAtEpochMillis) {
        if (token == null || token.isBlank()) return;
        long ttlMs = Math.max(0, expiresAtEpochMillis - System.currentTimeMillis());
        if (ttlMs == 0) return;

        String fingerprint = fingerprint(token);
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + fingerprint, "1", Duration.ofMillis(ttlMs));
            return;
        } catch (Exception ex) {
            if (production) {
                throw new IllegalStateException("Token revocation store is unavailable", ex);
            }
        }
        localBlacklist.put(fingerprint, expiresAtEpochMillis);
        cleanupLocal();
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        String fingerprint = fingerprint(token);
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + fingerprint))) return true;
        } catch (Exception ex) {
            if (production) {
                // Fail closed: while revocation state is unknown, do not trust the bearer token.
                return true;
            }
        }

        Long expiry = localBlacklist.get(fingerprint);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            localBlacklist.remove(fingerprint);
            return false;
        }
        return true;
    }

    private String fingerprint(String token) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private void cleanupLocal() {
        long now = System.currentTimeMillis();
        localBlacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
