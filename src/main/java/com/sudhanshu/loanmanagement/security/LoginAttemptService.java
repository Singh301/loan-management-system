package com.sudhanshu.loanmanagement.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts and locks accounts temporarily.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(normalize(username));
    }

    public void loginFailed(String username) {
        String key = normalize(username);
        AttemptInfo info = attempts.computeIfAbsent(key, k -> new AttemptInfo());
        info.failedAttempts++;
        if (info.failedAttempts >= MAX_ATTEMPTS) {
            info.lockedUntil = System.currentTimeMillis() + LOCK_DURATION_MS;
        }
    }

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(normalize(username));
        if (info == null || info.lockedUntil == 0) {
            return false;
        }
        if (System.currentTimeMillis() > info.lockedUntil) {
            attempts.remove(normalize(username));
            return false;
        }
        return true;
    }

    public int remainingAttempts(String username) {
        AttemptInfo info = attempts.get(normalize(username));
        if (info == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - info.failedAttempts);
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static class AttemptInfo {
        int failedAttempts;
        long lockedUntil;
    }
}
