package com.immortals.auth.service.security.oauth2;

import com.immortals.auth.service.security.UserPrincipal;
import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.service.CookieService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Called by Spring Security after a successful OAuth2 authorization code exchange.
 * Generates JWT cookies (identical to the form-login path) and redirects to the
 * frontend. The frontend URL is controlled by auth.oauth2.redirect-uri in config.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final ILogger log = LogManager.getInstance().getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtProvider jwtProvider;
    private final CookieService cookieService;

    @Value("${auth.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtProvider jwtProvider, CookieService cookieService) {
        this.jwtProvider = jwtProvider;
        this.cookieService = cookieService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        try {
            String accessToken = jwtProvider.generateAccessToken(auth);
            cookieService.addAccessTokenCookie(response, accessToken);
            log.info("OAuth2 login successful for '{}', JWT issued.", principal.getEmail());
        } catch (Exception e) {
            log.error("Failed to generate JWT after OAuth2 login for '{}': {}",
                    principal.getEmail(), e.getMessage(), e);
            getRedirectStrategy().sendRedirect(request, response,
                    UriComponentsBuilder.fromUriString(redirectUri)
                            .queryParam("error", "token_generation_failed").build().toUriString());
            return;
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("login", "success")
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

