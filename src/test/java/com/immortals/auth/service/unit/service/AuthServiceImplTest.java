package com.immortals.auth.service.unit.service;

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
import com.immortals.auth.service.service.CookieService;
import com.immortals.auth.service.service.LoginAttemptService;
import com.immortals.auth.service.service.TokenBlacklistService;
import com.immortals.auth.service.service.exception.AuthenticationException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.exception.UserException;
import com.immortals.auth.service.service.impl.AuthServiceImpl;
import com.immortals.auth.service.util.TestDataFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtProvider jwtProvider;
    @Mock LoginAttemptService loginAttemptService;
    @Mock CookieService cookieService;
    @Mock TokenBlacklistService tokenBlacklistService;
    @Mock UserRepository userRepository;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock NotificationEventPublisher eventPublisher;
    @Mock UserDetailsService userDetailsService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock HttpServletRequest httpRequest;
    @Mock HttpServletResponse httpResponse;

    @InjectMocks
    AuthServiceImpl authService;

    @BeforeEach
    void injectProperties() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604_800_000);
        ReflectionTestUtils.setField(authService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "frontendBaseUrl", "http://localhost:3000");
    }

    // ──────────────────────────── login ────────────────────────────

    @Test
    void login_withValidCredentials_noRememberMe_returnsTokenResponse() throws Exception {
        LoginRequestDto dto = TestDataFactory.buildLoginRequest();
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(loginAttemptService.isBlocked(dto.email())).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtProvider.generateAccessToken(auth)).thenReturn("access.token.here");

        LoginResponseDto response = authService.login(dto, httpResponse);

        assertThat(response.accessToken()).isEqualTo("access.token.here");
        assertThat(response.refreshToken()).isEmpty();
        verify(loginAttemptService).loginSucceeded(dto.email());
        verify(cookieService).addAccessTokenCookie(httpResponse, "access.token.here");
        verify(cookieService, never()).addRefreshTokenCookie(any(), any());
    }

    @Test
    void login_withRememberMe_addsRefreshTokenCookie() throws Exception {
        LoginRequestDto dto = TestDataFactory.buildLoginRequestWithRememberMe();
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(loginAttemptService.isBlocked(dto.email())).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtProvider.generateAccessToken(auth)).thenReturn("access.token");
        when(jwtProvider.generateRefreshToken(auth, 604_800_000)).thenReturn("refresh.token");

        LoginResponseDto response = authService.login(dto, httpResponse);

        assertThat(response.refreshToken()).isEqualTo("refresh.token");
        verify(cookieService).addRefreshTokenCookie(httpResponse, "refresh.token");
    }

    @Test
    void login_whenBlocked_throwsAuthenticationException() {
        LoginRequestDto dto = TestDataFactory.buildLoginRequest();
        when(loginAttemptService.isBlocked(dto.email())).thenReturn(true);
        when(loginAttemptService.getRemainingBlockTime(dto.email())).thenReturn(Duration.ofMinutes(10));

        assertThatThrownBy(() -> authService.login(dto, httpResponse))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("locked");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_whenBadCredentials_recordsFailureAndThrows() {
        LoginRequestDto dto = TestDataFactory.buildLoginRequest();
        when(loginAttemptService.isBlocked(dto.email())).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(dto, httpResponse))
                .isInstanceOf(AuthenticationException.class);

        verify(loginAttemptService).loginFailed(dto.email());
    }

    // ──────────────────────────── logout ────────────────────────────

    @Test
    void logout_withBothTokensPresent_blacklistsBothAndClearsCookies() throws Exception {
        when(cookieService.extractAccessToken(httpRequest)).thenReturn(Optional.of("access.token"));
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.of("refresh.token"));
        when(jwtProvider.getJti("access.token")).thenReturn("jti-access");
        when(jwtProvider.getJti("refresh.token")).thenReturn("jti-refresh");
        when(jwtProvider.getExpiryEpochSecond("access.token")).thenReturn(9999999L);
        when(jwtProvider.getExpiryEpochSecond("refresh.token")).thenReturn(9999999L);

        authService.logout(httpRequest, httpResponse);

        verify(tokenBlacklistService).blacklist("jti-access", 9999999L);
        verify(tokenBlacklistService).blacklist("jti-refresh", 9999999L);
        verify(cookieService).clearAuthCookies(httpResponse);
    }

    @Test
    void logout_withNoTokens_onlyClearsCookies() {
        when(cookieService.extractAccessToken(httpRequest)).thenReturn(Optional.empty());
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.empty());

        authService.logout(httpRequest, httpResponse);

        verify(tokenBlacklistService, never()).blacklist(anyString(), anyLong());
        verify(cookieService).clearAuthCookies(httpResponse);
    }

    // ──────────────────────────── refresh ────────────────────────────

    @Test
    void refresh_withValidToken_returnsNewAccessToken() throws Exception {
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.of("refresh.token"));
        when(jwtProvider.validateToken("refresh.token")).thenReturn(true);
        when(jwtProvider.getJti("refresh.token")).thenReturn("jti-123");
        when(tokenBlacklistService.isBlacklisted("jti-123")).thenReturn(false);
        when(jwtProvider.getUsernameFromToken("refresh.token")).thenReturn("testuser");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtProvider.generateAccessToken(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn("new.access.token");

        LoginResponseDto response = authService.refresh(httpRequest, httpResponse);

        assertThat(response.accessToken()).isEqualTo("new.access.token");
        verify(cookieService).addAccessTokenCookie(httpResponse, "new.access.token");
    }

    @Test
    void refresh_withNoRefreshCookie_throwsAuthenticationException() {
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(httpRequest, httpResponse))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("No refresh token");
    }

    @Test
    void refresh_withInvalidToken_throwsAuthenticationException() {
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.of("bad.token"));
        when(jwtProvider.validateToken("bad.token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(httpRequest, httpResponse))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void refresh_withBlacklistedToken_throwsAuthenticationException() {
        when(cookieService.extractRefreshToken(httpRequest)).thenReturn(Optional.of("refresh.token"));
        when(jwtProvider.validateToken("refresh.token")).thenReturn(true);
        when(jwtProvider.getJti("refresh.token")).thenReturn("jti-revoked");
        when(tokenBlacklistService.isBlacklisted("jti-revoked")).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh(httpRequest, httpResponse))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("revoked");
    }

    // ──────────────────────────── forgotPassword ────────────────────────────

    @Test
    void forgotPassword_forExistingUser_savesTokenAndPublishesEvent() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByEmail(TestDataFactory.TEST_EMAIL)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword(new ForgotPasswordRequest(TestDataFactory.TEST_EMAIL));

        verify(verificationTokenRepository).deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(eventPublisher).publishPasswordReset(eq(TestDataFactory.TEST_EMAIL), anyString());
    }

    @Test
    void forgotPassword_forUnknownEmail_silentlyIgnores() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("unknown@example.com"));

        verify(verificationTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishPasswordReset(any(), any());
    }

    // ──────────────────────────── resetPassword ────────────────────────────

    @Test
    void resetPassword_withValidToken_updatesPasswordAndMarksTokenUsed() {
        User user = TestDataFactory.buildUser();
        VerificationToken vt = TestDataFactory.buildPasswordResetToken(user);
        when(verificationTokenRepository.findByTokenAndType("valid-reset-token", VerificationToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(vt));
        when(passwordEncoder.encode("NewPass@1234!")).thenReturn("encoded-new-pass");

        authService.resetPassword(new ResetPasswordRequest("valid-reset-token", "NewPass@1234!", "NewPass@1234!"));

        verify(userRepository).updatePassword(any(), eq("encoded-new-pass"));
        verify(verificationTokenRepository).save(vt);
        assertThat(vt.used()).isTrue();
    }

    @Test
    void resetPassword_whenPasswordsMismatch_throwsUserException() {
        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("token", "Pass@1234!", "Different@1!")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Passwords do not match");

        verify(verificationTokenRepository, never()).findByTokenAndType(any(), any());
    }

    @Test
    void resetPassword_withExpiredToken_throwsAuthenticationException() {
        User user = TestDataFactory.buildUser();
        VerificationToken expiredVt = TestDataFactory.buildExpiredEmailToken(user);
        expiredVt = VerificationToken.builder()
                .token("expired-token")
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(user)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        when(verificationTokenRepository.findByTokenAndType("expired-token", VerificationToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(expiredVt));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("expired-token", "NewPass@1234!", "NewPass@1234!")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_withNonExistentToken_throwsResourceNotFoundException() {
        when(verificationTokenRepository.findByTokenAndType("ghost-token", VerificationToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("ghost-token", "NewPass@1234!", "NewPass@1234!")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}