package com.immortals.auth.service.security.oauth2;

import com.immortals.auth.service.domain.enums.AuthProvider;

import java.util.Map;

public final class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException(
                    "OAuth2 provider '" + registrationId + "' is not supported.");
        };
    }

    public static AuthProvider toAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GMAIL;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new IllegalArgumentException(
                    "Unknown OAuth2 registration id: " + registrationId);
        };
    }
}

