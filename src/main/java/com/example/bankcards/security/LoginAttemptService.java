package com.example.bankcards.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 5;
    private final long BLOCK_TIME = 10 * 60 * 1000;
    private Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private Map<String, Long> blockTime = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        if (!blockTime.containsKey(username)) return false;
        if (System.currentTimeMillis() > blockTime.get(username)) {
            blockTime.remove(username);
            attemptsCache.remove(username);
            return false;
        }
        return true;
    }

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        blockTime.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            blockTime.put(username, System.currentTimeMillis() + BLOCK_TIME);
        }
    }
}

