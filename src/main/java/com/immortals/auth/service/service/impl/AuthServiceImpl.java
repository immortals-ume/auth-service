package com.immortals.auth.service.service.impl;


import com.immortals.auth.service.db.annotation.ReadOnly;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.domain.event.UserAccountLockedEvent;
import com.immortals.auth.service.domain.event.UserAuthenticatedEvent;
import com.immortals.auth.service.domain.event.UserAuthenticationFailedEvent;
import com.immortals.auth.service.domain.event.UserLoggedOutEvent;
import com.immortals.auth.service.exception.AuthenticationException;
import com.immortals.auth.service.security.UserDetailsServiceImpl;
import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.service.AuthService;
import com.immortals.auth.service.service.LoginAttemptService;
import com.immortals.auth.service.utils.ValidationUtils;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtProvider jwtProvider;
    private final LoginAttemptService loginAttempt;

    @Value("${auth.refresh-token-expiry-ms}")
    private int refreshTokenExpiryMs;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @ReadOnly
    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.email();

        try {
            ValidationUtils.requireValidEmail(loginRequestDto.email());
            ValidationUtils.requireValidPassword(loginRequestDto.password());
            ValidationUtils.requireValidMobile(loginRequestDto.mobileNumber());

            if (loginAttempt.isBlocked(username)) {
                Duration remainingTime = loginAttempt.getRemainingBlockTime(username);
                log.warn("Login attempt blocked for user '{}', remaining block time: {}", username, remainingTime);
                throw new AuthenticationException(
                        String.format("Your account is temporarily locked due to too many failed login attempts. Please try again in %d minutes.",
                                remainingTime.toMinutes()));
            }

            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password())
                );
            } catch (AuthenticationException e) {
                loginAttempt.loginFailed(username);

                if (loginAttempt.isBlocked(username)) {
                    publishUserAccountLocked(username);
                }
                throw new AuthenticationException("Authentication failed. Please check your credentials.", e);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (!authentication.isAuthenticated()) {
                loginAttempt.loginFailed(username);
                throw new AuthenticationException("Authentication failed. Please check your credentials.");
            }

            loginAttempt.loginSucceeded(username);

            String accessToken = jwtProvider.generateAccessToken(authentication);

            log.info("User '{}' successfully authenticated.", loginRequestDto.email());

            String refreshToken = "";

            if (loginRequestDto.rememberMe()) {
                refreshToken = jwtProvider.generateRefreshToken(authentication, refreshTokenExpiryMs);
                log.info("Refresh token generated and cached for user '{}'.", loginRequestDto.email());
            }

            return new LoginResponseDto(loginRequestDto.email(), accessToken, refreshToken, "Successfully generated the Login Token");

        } catch (RuntimeException | IOException | NoSuchAlgorithmException | InvalidKeySpecException |
                 JOSEException e) {
            log.error("Login failed for user '{}': {}", loginRequestDto.email(), e.getMessage(), e);
            throw new AuthenticationException("Login failed. Please verify your credentials and try again.", e);
        }
    }
}
