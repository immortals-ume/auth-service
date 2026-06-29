package com.immortals.auth.service.util;

import com.immortals.auth.service.domain.dto.AssignPermissionRequest;
import com.immortals.auth.service.domain.dto.AssignRoleRequest;
import com.immortals.auth.service.domain.dto.CreatePermissionDto;
import com.immortals.auth.service.domain.dto.CreateRoleRequest;
import com.immortals.auth.service.domain.dto.ForgotPasswordRequest;
import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.RegisterRequestDTO;
import com.immortals.auth.service.domain.dto.ResetCredentials;
import com.immortals.auth.service.domain.dto.ResetPasswordRequest;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.entity.VerificationToken;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;

public final class TestDataFactory {

    public static final String TEST_EMAIL        = "john.doe@example.com";
    public static final String TEST_USERNAME     = "johndoe";
    public static final String TEST_FIRST_NAME   = "John";
    public static final String TEST_LAST_NAME    = "Doe";
    public static final String TEST_PASSWORD_RAW = "Test@1234!";
    public static final String TEST_PHONE_CODE   = "+91";
    public static final String TEST_PHONE        = "9876543210";

    public static final String ADMIN_EMAIL       = "admin@example.com";
    public static final String ADMIN_USERNAME    = "adminuser";
    public static final String ADMIN_PASSWORD    = "Admin@1234!";

    private TestDataFactory() {}

    public static User buildUser() {
        return User.builder()
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .userName(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password("{bcrypt}encodedPassword")
                .emailVerified(false)
                .phoneNumberVerified(false)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .accountLocked(false)
                .credentialsNonExpired(true)
                .activeInd(true)
                .phoneCode(TEST_PHONE_CODE)
                .contactNumber(TEST_PHONE)
                .roles(new HashSet<>())
                .build();
    }

    public static User buildVerifiedUser() {
        return User.builder()
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .userName(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password("{bcrypt}encodedPassword")
                .emailVerified(true)
                .phoneNumberVerified(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .accountLocked(false)
                .credentialsNonExpired(true)
                .activeInd(true)
                .phoneCode(TEST_PHONE_CODE)
                .contactNumber(TEST_PHONE)
                .roles(new HashSet<>())
                .build();
    }

    public static RegisterRequestDTO buildRegisterRequest() {
        return new RegisterRequestDTO(
                TEST_FIRST_NAME, null, TEST_LAST_NAME,
                TEST_USERNAME, TEST_EMAIL,
                TEST_PASSWORD_RAW, TEST_PASSWORD_RAW,
                TEST_PHONE_CODE, TEST_PHONE
        );
    }

    public static RegisterRequestDTO buildRegisterRequest(String email, String username) {
        return new RegisterRequestDTO(
                TEST_FIRST_NAME, null, TEST_LAST_NAME,
                username, email,
                TEST_PASSWORD_RAW, TEST_PASSWORD_RAW,
                TEST_PHONE_CODE, TEST_PHONE
        );
    }

    public static LoginRequestDto buildLoginRequest() {
        return new LoginRequestDto(
                TEST_USERNAME, TEST_EMAIL, null,
                TEST_PASSWORD_RAW, false, null
        );
    }

    public static LoginRequestDto buildLoginRequestWithRememberMe() {
        return new LoginRequestDto(
                TEST_USERNAME, TEST_EMAIL, null,
                TEST_PASSWORD_RAW, true, null
        );
    }

    public static ForgotPasswordRequest buildForgotPasswordRequest() {
        return new ForgotPasswordRequest(TEST_EMAIL);
    }

    public static ResetPasswordRequest buildResetPasswordRequest(String token) {
        return new ResetPasswordRequest(token, "NewPass@1234!", "NewPass@1234!");
    }

    public static ResetCredentials buildResetCredentials() {
        return new ResetCredentials(TEST_USERNAME, TEST_PASSWORD_RAW, "NewPass@1234!", "NewPass@1234!");
    }

    public static Roles buildRole(String name) {
        return Roles.builder()
                .roleName(name)
                .description("Test role: " + name)
                .activeInd(true)
                .build();
    }

    public static Permissions buildPermission(String name) {
        return Permissions.builder()
                .name(name)
                .description("Test permission: " + name)
                .build();
    }

    public static VerificationToken buildEmailToken(User user) {
        return VerificationToken.builder()
                .token("valid-email-token")
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build();
    }

    public static VerificationToken buildPasswordResetToken(User user) {
        return VerificationToken.builder()
                .token("valid-reset-token")
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();
    }

    public static VerificationToken buildExpiredEmailToken(User user) {
        return VerificationToken.builder()
                .token("expired-email-token")
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().minus(Duration.ofHours(1)))
                .build();
    }

    public static CreateRoleRequest buildCreateRoleRequest(String name) {
        return new CreateRoleRequest(name, "Test role");
    }

    public static CreatePermissionDto buildCreatePermissionDto(String name) {
        return new CreatePermissionDto(name, "Test permission");
    }

    public static AssignRoleRequest buildAssignRoleRequest(Long userId, Long roleId) {
        return new AssignRoleRequest(userId, roleId);
    }

    public static AssignPermissionRequest buildAssignPermissionRequest(Long roleId, Long permissionId) {
        return new AssignPermissionRequest(roleId, permissionId);
    }
}