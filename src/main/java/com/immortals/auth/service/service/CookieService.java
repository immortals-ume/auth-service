package com.immortals.auth.service.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface CookieService {

    void addAccessTokenCookie(HttpServletResponse response, String token);

    void addRefreshTokenCookie(HttpServletResponse response, String token);

    void clearAuthCookies(HttpServletResponse response);

    Optional<String> extractAccessToken(HttpServletRequest request);

    Optional<String> extractRefreshToken(HttpServletRequest request);
}
