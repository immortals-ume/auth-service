package com.immortals.auth.service.security.oauth2;

import java.util.Map;

/**
 * Provider-agnostic view of the user attributes returned by the OAuth2 provider.
 * Each social provider returns different attribute keys — this interface normalises them.
 */
public interface OAuth2UserInfo {
    String getProviderId();
    String getName();
    String getEmail();
    String getImageUrl();
    Map<String, Object> getAttributes();
}

