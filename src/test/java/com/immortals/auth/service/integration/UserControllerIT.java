package com.immortals.auth.service.integration;

import com.immortals.auth.service.domain.dto.RegisterRequestDTO;
import com.immortals.auth.service.domain.dto.ResetCredentials;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired VerificationTokenRepository verificationTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedTestUser() {
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
        userRepository.save(user);
    }

    // ──────────────────────────── POST /api/v1/user/register ────────────────────────────

    @Test
    void register_withValidData_returns201() throws Exception {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest("newuser@example.com", "newuser");

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.success").value(true));

        verify(notificationEventPublisher).publishEmailVerification(anyString(), anyString());
    }

    @Test
    void register_withExistingEmail_returns409() throws Exception {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest(
                TestDataFactory.TEST_EMAIL, "anotheruser");

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withExistingUsername_returns409() throws Exception {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest(
                "another@email.com", TestDataFactory.TEST_USERNAME);

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "John", null, "Doe", "johndoe2", "not-an-email",
                TestDataFactory.TEST_PASSWORD_RAW, TestDataFactory.TEST_PASSWORD_RAW,
                "+91", TestDataFactory.TEST_PHONE);

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    void register_withWeakPassword_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "John", null, "Doe", "johndoe3", "strong@email.com",
                "weak", "weak", "+91", TestDataFactory.TEST_PHONE);

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenPasswordsMismatch_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "John", null, "Doe", "johndoe4", "john@email.com",
                "Test@1234!", "Different@456!", "+91", TestDataFactory.TEST_PHONE);

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withMissingFirstName_returns400() throws Exception {
        String body = """
                {
                    "lastName": "Doe",
                    "userName": "jdoe99",
                    "email": "jdoe@example.com",
                    "password": "Test@1234!",
                    "retypedPassword": "Test@1234!",
                    "phoneCode": "+91",
                    "contactNumber": "9876543210"
                }
                """;

        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── GET /api/v1/user/me ────────────────────────────

    @Test
    @WithMockUser(username = TestDataFactory.TEST_USERNAME)
    void getProfile_whenAuthenticated_returns200WithUserData() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(TestDataFactory.TEST_EMAIL))
                .andExpect(jsonPath("$.data.fullName").value(
                        TestDataFactory.TEST_FIRST_NAME + " " + TestDataFactory.TEST_LAST_NAME));
    }

    @Test
    void getProfile_whenUnauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────── GET /api/v1/user/verify-email/confirm ────────────────────────────

    @Test
    void verifyEmail_withValidToken_returns200() throws Exception {
        User user = userRepository.findByUserName(TestDataFactory.TEST_USERNAME).orElseThrow();
        VerificationToken vt = VerificationToken.builder()
                .token("email-verify-token")
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build();
        verificationTokenRepository.save(vt);

        mockMvc.perform(get("/api/v1/user/verify-email/confirm")
                        .param("token", "email-verify-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void verifyEmail_withMissingToken_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/user/verify-email/confirm"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmail_withExpiredToken_returns401() throws Exception {
        User user = userRepository.findByUserName(TestDataFactory.TEST_USERNAME).orElseThrow();
        VerificationToken expired = VerificationToken.builder()
                .token("expired-verify-token")
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().minus(Duration.ofHours(2)))
                .build();
        verificationTokenRepository.save(expired);

        mockMvc.perform(get("/api/v1/user/verify-email/confirm")
                        .param("token", "expired-verify-token"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────── PUT /api/v1/user/change-password ────────────────────────────

    @Test
    @WithMockUser(username = TestDataFactory.TEST_USERNAME)
    void changePassword_withCorrectCredentials_returns204() throws Exception {
        ResetCredentials creds = new ResetCredentials(
                TestDataFactory.TEST_USERNAME,
                TestDataFactory.TEST_PASSWORD_RAW,
                "NewPass@1234!",
                "NewPass@1234!");

        mockMvc.perform(put("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TestDataFactory.TEST_USERNAME)
    void changePassword_withWrongOldPassword_returns401() throws Exception {
        ResetCredentials creds = new ResetCredentials(
                TestDataFactory.TEST_USERNAME,
                "WrongOld@1!",
                "NewPass@1234!",
                "NewPass@1234!");

        mockMvc.perform(put("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_whenUnauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
