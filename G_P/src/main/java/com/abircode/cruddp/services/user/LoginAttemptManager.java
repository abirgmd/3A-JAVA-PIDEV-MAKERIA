package com.abircode.cruddp.services.user;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LoginAttemptManager {
    private static final LoginAttemptManager INSTANCE = new LoginAttemptManager();
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 10;

    public LoginAttemptManager() {}

    public static LoginAttemptManager getInstance() {
        return INSTANCE;
    }

    public void recordFailedAttempt(String email) {
        AttemptInfo info = attempts.computeIfAbsent(email, k -> new AttemptInfo());
        info.incrementAttempts();
    }

    public void resetAttempts(String email) {
        attempts.remove(email);
    }

    public boolean isBlocked(String email) {
        AttemptInfo info = attempts.get(email);
        return info != null && info.isBlocked();
    }

    public int getRemainingAttempts(String email) {
        AttemptInfo info = attempts.get(email);
        return info == null ? MAX_ATTEMPTS : MAX_ATTEMPTS - info.getAttemptCount();
    }

    public long getRemainingBlockTime(String email) {
        AttemptInfo info = attempts.get(email);
        if (info == null || !info.isBlocked()) return 0;

        long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - info.getBlockTime()
        );
        return BLOCK_DURATION_MINUTES - elapsedMinutes;
    }

    private static class AttemptInfo {
        private int attempts;
        private long blockTime;

        public void incrementAttempts() {
            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                blockTime = System.currentTimeMillis();
            }
        }

        public boolean isBlocked() {
            if (attempts < MAX_ATTEMPTS) return false;

            long elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(
                    System.currentTimeMillis() - blockTime
            );
            return elapsedMinutes < BLOCK_DURATION_MINUTES;
        }

        public int getAttemptCount() {
            return attempts;
        }

        public long getBlockTime() {
            return blockTime;
        }
    }
}