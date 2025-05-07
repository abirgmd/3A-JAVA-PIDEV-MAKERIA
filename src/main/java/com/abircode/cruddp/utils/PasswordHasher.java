package com.abircode.cruddp.utils;

import org.mindrot.jbcrypt.BCrypt;


public class PasswordHasher {
    /**
     * Generates a BCrypt hash compatible with Symfony's default configuration
     * @param plainPassword The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(13));
    }

    /**
     * Verifies a password against a Symfony-generated hash
     * @param plainPassword The password to check
     * @param hashedPassword The hash from database
     * @return true if the password matches
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2")) {
            return false;
        }

        // Convert Symfony's 2y prefix to 2a for Java compatibility
        if (hashedPassword.startsWith("$2y$")) {
            hashedPassword = "$2a$" + hashedPassword.substring(4);
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if a hash needs rehashing (for password upgrades)
     * @param hashedPassword The existing hash
     * @return true if hash should be regenerated
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) return true;

        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2y$")) {
            return true;
        }

        try {
            int cost = Integer.parseInt(hashedPassword.substring(4, 6));
            return cost < 13;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}