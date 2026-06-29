package com.immortals.auth.service.integration;

import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.entity.VerificationToken;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.repository.VerificationTokenRepository;
import com.immortals.auth.service.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired VerificationTokenRepository verificationTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User savedUser;

    @BeforeEach
    void seedUser() {
        doNothing().when(notificationEventPublisher).publishEmailVerification(anyString(), anyString());
        User user = User.builder()
                .firstName(TestDataFactory.TEST_FIRST_NAME)
                .lastName(TestDataFactory.TEST_LAST_NAME)
                .userName(TestDataFactory.TEST_USERNAME)
                .email(TestDataFactory.TEST_EMAIL)
                .password(passwordEncoder.encode(TestDataFactory.TEST_PASSWORD_RAW))
                .emailVerified(true)
                .phoneNumberVerified(false)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .accountLocked(false)
                .credentialsNonExpired(true)
                .activeInd(true)
                .phoneCode(TestDataFactory.TEST_PHONE_CODE)
                .contactNumber(TestDataFactory.TEST_PHONE)
                .build();
        savedUser = userRepository.save(user);
    }


    @Test
    void login_withValidCredentials_returns200AndAccessToken() throws Exception {
        LoginRequestDto body = TestDataFactory.buildLoginRequest();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.username").value(TestDataFactory.TEST_EMAIL))
                .andExpect(cookie().exists("access_token"));
    }

    @Test
    void login_withRememberMe_setsRefreshTokenCookie() throws Exception {
        LoginRequestDto body = TestDataFactory.buildLoginRequestWithRememberMe();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        LoginRequestDto body = new LoginRequestDto(
                TestDataFactory.TEST_USERNAME, TestDataFactory.TEST_EMAIL,
                null, "WrongPass@1!", false, null);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBlankEmail_returns400() throws Exception {
        String body = """
                {"email": "", "password": "Test@1234!"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withInvalidEmailFormat_returns400() throws Exception {
        String body = """
                {"email": "not-an-email", "password": "Test@1234!"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withMissingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── POST /api/v1/auth/logout ────────────────────────────

    @Test
    void logout_withoutCookies_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    // ──────────────────────────── POST /api/v1/auth/forgot-password ────────────────────────────

    @Test
    void forgotPassword_withExistingEmail_returns200WithGenericMessage() throws Exception {
        doNothing().when(notificationEventPublisher).publishPasswordReset(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ForgotPasswordRequest(TestDataFactory.TEST_EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If that email is registered, a reset link has been sent."));
    }

    @Test
    void forgotPassword_withUnknownEmail_returns200SameMessage() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ForgotPasswordRequest("nobody@unknown.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "If that email is registered, a reset link has been sent."));
    }

    @Test
    void forgotPassword_withBlankEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── POST /api/v1/auth/reset-password ────────────────────────────

    @Test
    void resetPassword_withValidToken_returns204() throws Exception {
        VerificationToken vt = VerificationToken.builder()
                .token("reset-token-123")
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(savedUser)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();
        verificationTokenRepository.save(vt);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("reset-token-123", "NewPass@1234!", "NewPass@1234!"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_whenPasswordsMismatch_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("some-token", "NewPass@1234!", "Different@456!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_withExpiredToken_returns401() throws Exception {
        VerificationToken expired = VerificationToken.builder()
                .token("expired-reset-token")
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(savedUser)
                .expiresAt(Instant.now().minus(Duration.ofHours(2)))
                .build();
        verificationTokenRepository.save(expired);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("expired-reset-token", "NewPass@1234!", "NewPass@1234!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resetPassword_withNonExistentToken_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("ghost-token", "NewPass@1234!", "NewPass@1234!"))))
                .andExpect(status().isNotFound());
    }
}
