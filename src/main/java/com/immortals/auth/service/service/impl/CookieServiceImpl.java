package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.domain.CookieProperties;
import com.immortals.auth.service.service.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieServiceImpl implements CookieService {

    private final CookieProperties props;

    public CookieServiceImpl(CookieProperties props) {
        this.props = props;
    }

    /**
     * Writes the access token as a regular (JS-readable) cookie.
     * SameSite=Strict prevents CSRF for same-origin apps.
     * Not HttpOnly so the frontend can read and attach it as a Bearer header if needed.
     */
    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(props.accessTokenName(), token)
                .domain(props.domain())
                .path(props.path())
                .secure(props.secure())
                .maxAge(props.accessTokenMaxAgeSeconds())
                .sameSite("Strict")
                .httpOnly(props.httpOnly())
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Writes the refresh token as a strict HttpOnly cookie.
     * JS cannot read this — only the browser sends it automatically.
     * SameSite=Strict means it won't be sent on cross-site requests.
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(props.refreshTokenName(), token)
                .domain(props.domain())
                .path(props.path())
                .secure(props.secure())
                .httpOnly(props.httpOnly())
                .maxAge(props.refreshTokenMaxAgeSeconds())
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Overwrites both cookies with empty values and maxAge=0,
     * which instructs the browser to delete them immediately.
     */
    public void clearAuthCookies(HttpServletResponse response) {
        clearCookie(response, props.accessTokenName(), false);
        clearCookie(response, props.refreshTokenName(), true);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, props.accessTokenName());
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, props.refreshTokenName());
    }

    private void clearCookie(HttpServletResponse response, String name, boolean httpOnly) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .domain(props.domain())
                .path(props.path())
                .secure(props.secure())
                .httpOnly(httpOnly)
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private Optional<String> extractCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}