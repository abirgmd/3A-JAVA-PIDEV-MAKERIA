package com.abircode.cruddp.utils;


import com.abircode.cruddp.entities.User;

public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearSession() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null &&
                currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_ADMIN");
    }

    public static boolean isArtist() {
        return currentUser != null &&
                currentUser.getRoles() != null &&
                currentUser.getRoles().contains("ROLE_ARTIST");
    }
}