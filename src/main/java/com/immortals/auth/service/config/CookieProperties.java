package com.immortals.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.cookie")
public record CookieProperties(
        String accessTokenName,
        String refreshTokenName,
        String domain,
        String path,
        boolean secure,
        int accessTokenMaxAgeSeconds,
        int refreshTokenMaxAgeSeconds,
        boolean httpOnly
) {}

