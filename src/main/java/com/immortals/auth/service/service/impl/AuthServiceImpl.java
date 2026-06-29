package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.db.annotation.WriteOnly;
import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.entity.VerificationToken;
import com.immortals.auth.service.event.NotificationEventPublisher;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.repository.VerificationTokenRepository;
import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.service.AuthService;
import com.immortals.auth.service.service.CookieService;
import com.immortals.auth.service.service.LoginAttemptService;
import com.immortals.auth.service.service.TokenBlacklistService;
import com.immortals.auth.service.service.exception.AuthenticationException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.exception.UserException;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final ILogger log = LogManager.getInstance().getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final LoginAttemptService loginAttempt;
    private final CookieService cookieService;
    private final TokenBlacklistService blacklistService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationEventPublisher eventPublisher;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.refresh-token-expiry-ms}")
    private int refreshTokenExpiryMs;

    @Value("${auth.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${auth.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtProvider jwtProvider,
            LoginAttemptService loginAttempt,
            CookieService cookieService,
            TokenBlacklistService blacklistService,
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            NotificationEventPublisher eventPublisher,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.loginAttempt = loginAttempt;
        this.cookieService = cookieService;
        this.blacklistService = blacklistService;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.eventPublisher = eventPublisher;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @WriteOnly
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto,
                                  HttpServletResponse response) {
        String username = loginRequestDto.email();

        try {
            if (loginAttempt.isBlocked(username)) {
                Duration remaining = loginAttempt.getRemainingBlockTime(username);
                log.warn("Login blocked for '{}', remaining: {}", username, remaining);
                throw new AuthenticationException(
                        String.format(
                                "Account temporarily locked. Try again in %d minute(s).",
                                remaining.toMinutes()));
            }
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDto.email(),
                                loginRequestDto.password()));
            } catch (org.springframework.security.core.AuthenticationException e) {
                loginAttempt.loginFailed(username);
                throw new AuthenticationException(
                        "Authentication failed. Please check your credentials.", e);
            }

            if (!authentication.isAuthenticated()) {
                loginAttempt.loginFailed(username);
                throw new AuthenticationException(
                        "Authentication failed. Please check your credentials.");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            loginAttempt.loginSucceeded(username);

            String accessToken = jwtProvider.generateAccessToken(authentication);
            cookieService.addAccessTokenCookie(response, accessToken);

            String refreshToken = "";
            if (Boolean.TRUE.equals(loginRequestDto.rememberMe())) {
                refreshToken = jwtProvider.generateRefreshToken(authentication, refreshTokenExpiryMs);
                cookieService.addRefreshTokenCookie(response, refreshToken);
                log.info("Refresh token cookie set for '{}'.", username);
            }

            log.info("User '{}' authenticated successfully.", username);
            return new LoginResponseDto(loginRequestDto.email(), accessToken, refreshToken, "Login successful");

        } catch (AuthenticationException e) {
            throw e;
        } catch (RuntimeException | JOSEException e) {
            log.error("Login failed for '{}': {}", username, e.getMessage(), e);
            throw new AuthenticationException("Login failed. Please verify your credentials and try again.", e);
        }
    }

    @WriteOnly
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.extractAccessToken(request).ifPresent(token -> {
            try {
                String jti = jwtProvider.getJti(token);
                long expiry = jwtProvider.getExpiryEpochSecond(token);
                blacklistService.blacklist(jti, expiry);
                log.info("Access token blacklisted (jti={}).", jti);
            } catch (Exception ex) {
                log.warn("Could not blacklist access token: {}", ex.getMessage());
            }
        });

        cookieService.extractRefreshToken(request).ifPresent(token -> {
            try {
                String jti = jwtProvider.getJti(token);
                long expiry = jwtProvider.getExpiryEpochSecond(token);
                blacklistService.blacklist(jti, expiry);
                log.info("Refresh token blacklisted (jti={}).", jti);
            } catch (Exception ex) {
                log.warn("Could not blacklist refresh token: {}", ex.getMessage());
            }
        });

        cookieService.clearAuthCookies(response);
        SecurityContextHolder.clearContext();
        log.info("User logged out — cookies cleared and tokens blacklisted.");
    }

    @Override
    public LoginResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshToken(request)
                .orElseThrow(() -> new AuthenticationException("No refresh token found"));

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthenticationException("Refresh token is invalid or expired");
        }

        String jti = jwtProvider.getJti(refreshToken);
        if (jti != null && blacklistService.isBlacklisted(jti)) {
            throw new AuthenticationException("Refresh token has been revoked");
        }

        try {
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            String newAccessToken = jwtProvider.generateAccessToken(auth);
            cookieService.addAccessTokenCookie(response, newAccessToken);

            log.info("Access token refreshed for '{}'.", username);
            return new LoginResponseDto(username, newAccessToken, "", "Token refreshed");

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            throw new AuthenticationException("Token refresh failed", e);
        }
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.email();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.warn("Forgot-password request for unknown email: {}", email);
            return;
        }

        verificationTokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);

        String tokenValue = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(tokenValue)
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();
        verificationTokenRepository.save(vt);

        String resetLink = frontendBaseUrl + "/reset-password?token=" + tokenValue;
        eventPublisher.publishPasswordReset(email, resetLink);
        log.info("Password reset event published for '{}'.", email);
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        if (!resetPasswordRequest.newPassword().equals(resetPasswordRequest.confirmPassword())) {
            throw new UserException("Passwords do not match");
        }

        VerificationToken vt = verificationTokenRepository
                .findByTokenAndType(resetPasswordRequest.token(), VerificationToken.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        if (vt.isExpired() || vt.used()) {
            throw new AuthenticationException("Reset token has expired or already been used");
        }

        userRepository.updatePassword(
                vt.user().getId(),
                passwordEncoder.encode(resetPasswordRequest.newPassword()));

        vt.markUsed();
        verificationTokenRepository.save(vt);

        log.info("Password reset successfully for user id={}.", vt.user().getId());
    }
}

