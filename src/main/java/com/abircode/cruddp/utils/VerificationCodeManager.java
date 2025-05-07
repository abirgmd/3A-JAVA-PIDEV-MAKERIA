package com.abircode.cruddp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class VerificationCodeManager {
    private static final Map<String, String> emailToCodeMap = new HashMap<>();
    private static final Map<String, Long> codeExpirationMap = new HashMap<>();
    private static final long CODE_EXPIRATION_TIME = 15 * 60 * 1000; // 15 minutes

    public static CompletableFuture<String> generateAndStoreCode(String email) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Generate random code
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(999999));

        // Store code immediately (before sending email)
        emailToCodeMap.put(email, code);
        codeExpirationMap.put(email, System.currentTimeMillis() + CODE_EXPIRATION_TIME);

        // Send email asynchronously
        EmailSender.sendVerificationEmailAsync(email, code, success -> {
            if (success) {
                future.complete(code);
            } else {
                // Remove code if email failed to send
                emailToCodeMap.remove(email);
                codeExpirationMap.remove(email);
                future.completeExceptionally(new RuntimeException("Failed to send email"));
            }
        });

        return future;
    }

    public static boolean verifyCode(String email, String code) {
        String storedCode = emailToCodeMap.get(email);
        Long expirationTime = codeExpirationMap.get(email);

        if (storedCode == null || expirationTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expirationTime) {
            emailToCodeMap.remove(email);
            codeExpirationMap.remove(email);
            return false;
        }

        return storedCode.equals(code);
    }

    public static void removeCode(String email) {
        emailToCodeMap.remove(email);
        codeExpirationMap.remove(email);
    }
}