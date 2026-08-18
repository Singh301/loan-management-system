package com.sudhanshu.loanmanagement.security;

import org.springframework.beans.factory.annotation.Autowired;
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
 * DEV: in-memory fallback (Redis optional).
 * PROD: Redis is mandatory; failures fail closed.
 */
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "loan:blacklist:";
    private final Map<String, Long> localBlacklist = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate; // may be null in dev
    private final boolean production;

    public TokenBlacklistService(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            @Value("${spring.profiles.active:dev}") String activeProfile) {
        this.redisTemplate = redisTemplate;
        this.production = activeProfile != null && activeProfile.contains("prod");
    }

    public void blacklist(String token, long expiresAtEpochMillis) {
        if (token == null || token.isBlank()) return;
        long ttlMs = Math.max(0, expiresAtEpochMillis - System.currentTimeMillis());
        if (ttlMs == 0) return;

        String fingerprint = fingerprint(token);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                        KEY_PREFIX + fingerprint, "1", Duration.ofMillis(ttlMs));
                return;
            } catch (Exception ex) {
                if (production) {
                    throw new IllegalStateException("Token revocation store is unavailable", ex);
                }
            }
        }

        // Dev fallback (or Redis failure in non-prod)
        localBlacklist.put(fingerprint, expiresAtEpochMillis);
        cleanupLocal();
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        String fingerprint = fingerprint(token);

        if (redisTemplate != null) {
            try {
                if (Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + fingerprint))) {
                    return true;
                }
            } catch (Exception ex) {
                if (production) {
                    // Fail closed in production
                    return true;
                }
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
                    MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private void cleanupLocal() {
        long now = System.currentTimeMillis();
        localBlacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}