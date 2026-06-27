package com.immortals.auth.service.security.filters;

import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.service.CookieService;
import com.immortals.auth.service.service.TokenBlacklistService;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

import static com.immortals.auth.service.constant.AuthConstants.HEADER_STRING;
import static com.immortals.auth.service.constant.AuthConstants.TOKEN_PREFIX;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final ILogger log = LogManager.getInstance().getLogger(JwtAuthenticationFilter.class);
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final CookieService cookieService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserDetailsService userDetailsService,
            CookieService cookieService,
            TokenBlacklistService tokenBlacklistService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.cookieService = cookieService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtProvider.validateToken(token)) {

                String jti = jwtProvider.getJti(token);
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.warn("Rejected blacklisted token (jti={})", jti);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked");
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String username = jwtProvider.getUsernameFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (RuntimeException ex) {
            log.error("JWT authentication failed: {}", ex.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(bearer) && bearer.startsWith(TOKEN_PREFIX)) {
            return bearer.substring(TOKEN_PREFIX.length());
        }
        return cookieService.extractAccessToken(request).orElse(null);
    }
}