package com.abircode.cruddp.utils;


import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {

    private static final String CLIENT_ID = "1057382569672-rn0qrfgjq6k61jmjf5pcfahd4din5srv.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    private static GoogleAuthorizationCodeFlow flow;

    static {
        try {
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    CLIENT_ID,
                    CLIENT_SECRET,
                    SCOPES)
                    .setAccessType("offline")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAuthorizationUrl() {
        return flow.newAuthorizationUrl().setRedirectUri("http://localhost:8080/Callback").build();
    }

    public static User authenticateWithGoogle(String authorizationCode, UserService userService) throws IOException, SQLException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri("http://localhost")  // Just "http://localhost"
                .execute();

        // Get user info from Google
        com.google.api.services.oauth2.Oauth2 oauth2 = new com.google.api.services.oauth2.Oauth2.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                null)
                .setApplicationName("Your Application Name")
                .build();

        Userinfo userInfo = oauth2.userinfo()
                .get()
                .setOauthToken(tokenResponse.getAccessToken())
                .execute();

        // Create or get user from your database
        User existingUser = userService.getUserByEmail(userInfo.getEmail());

        if (existingUser == null) {
            // Create new user
            User newUser = new User();
            newUser.setEmail(userInfo.getEmail());
            newUser.setLastname(userInfo.getFamilyName());
            newUser.setName(userInfo.getGivenName());
            newUser.setRoles("ROLE_USER");
            newUser.setBlok(false);
            newUser.setPassword("google_auth"); // No password for Google-authenticated users

            userService.createUser(newUser);
            return newUser;
        } else {
            return existingUser;
        }
    }
}