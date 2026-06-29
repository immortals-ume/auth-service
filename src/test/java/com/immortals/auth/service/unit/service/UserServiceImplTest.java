package com.immortals.auth.service.unit.service;

import com.immortals.auth.service.domain.dto.RegisterRequestDTO;
import com.immortals.auth.service.domain.dto.RegisterResponseDTO;
import com.immortals.auth.service.domain.dto.ResetCredentials;
import com.immortals.auth.service.domain.dto.UserResponseDTO;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.entity.VerificationToken;
import com.immortals.auth.service.event.NotificationEventPublisher;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.repository.VerificationTokenRepository;
import com.immortals.auth.service.service.OtpService;
import com.immortals.auth.service.service.exception.AuthenticationException;
import com.immortals.auth.service.service.exception.DuplicateResourceException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.exception.UserException;
import com.immortals.auth.service.service.impl.UserServiceImpl;
import com.immortals.auth.service.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock OtpService otpService;
    @Mock NotificationEventPublisher eventPublisher;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void injectProperties() {
        ReflectionTestUtils.setField(userService, "baseUrl", "http://localhost:8080");
        setupSecurityContext(TestDataFactory.TEST_USERNAME);
    }

    private void setupSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(username);
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ──────────────────────────── register ────────────────────────────

    @Test
    void register_withValidData_createsUserAndSendsVerification() {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByUserName(dto.userName())).thenReturn(false);
        when(passwordEncoder.encode(dto.password())).thenReturn("hashed-pw");

        User savedUser = TestDataFactory.buildUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(verificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponseDTO response = userService.register(dto);

        assertThat(response.username()).isEqualTo(TestDataFactory.TEST_USERNAME);
        assertThat(response.message()).isNotBlank();

        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(eventPublisher).publishEmailVerification(anyString(), anyString());
    }

    @Test
    void register_whenPasswordsMismatch_throwsUserException() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "John", null, "Doe", "johndoe", "j@example.com",
                "Pass@1234!", "Different@1!", "+91", "9876543210"
        );

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenEmailAlreadyExists_throwsDuplicateResourceException() {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenUsernameAlreadyExists_throwsDuplicateResourceException() {
        RegisterRequestDTO dto = TestDataFactory.buildRegisterRequest();
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByUserName(dto.userName())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
    }

    // ──────────────────────────── verifyEmail ────────────────────────────

    @Test
    void verifyEmail_withValidToken_marksEmailVerifiedAndSavesToken() {
        User user = TestDataFactory.buildUser();
        VerificationToken vt = TestDataFactory.buildEmailToken(user);
        when(verificationTokenRepository.findByTokenAndType("valid-email-token", VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(vt));

        userService.verifyEmail("valid-email-token");

        verify(userRepository).markEmailVerified(any());
        verify(verificationTokenRepository).save(vt);
        assertThat(vt.used()).isTrue();
    }

    @Test
    void verifyEmail_withExpiredToken_throwsAuthenticationException() {
        User user = TestDataFactory.buildUser();
        VerificationToken expiredVt = VerificationToken.builder()
                .token("expired-token")
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
        when(verificationTokenRepository.findByTokenAndType("expired-token", VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(expiredVt));

        assertThatThrownBy(() -> userService.verifyEmail("expired-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyEmail_withAlreadyUsedToken_throwsAuthenticationException() {
        User user = TestDataFactory.buildUser();
        VerificationToken usedVt = TestDataFactory.buildEmailToken(user);
        usedVt.markUsed();
        when(verificationTokenRepository.findByTokenAndType("valid-email-token", VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(usedVt));

        assertThatThrownBy(() -> userService.verifyEmail("valid-email-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyEmail_withNonExistentToken_throwsResourceNotFoundException() {
        when(verificationTokenRepository.findByTokenAndType("ghost-token", VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("ghost-token"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────────────── sendEmailVerification ────────────────────────────

    @Test
    void sendEmailVerification_whenEmailNotVerified_resendToken() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.sendEmailVerification();

        verify(eventPublisher).publishEmailVerification(anyString(), anyString());
    }

    @Test
    void sendEmailVerification_whenEmailAlreadyVerified_skipsResend() {
        User user = TestDataFactory.buildVerifiedUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));

        userService.sendEmailVerification();

        verify(eventPublisher, never()).publishEmailVerification(any(), any());
    }

    // ──────────────────────────── getProfile ────────────────────────────

    @Test
    void getProfile_returnsCorrectUserData() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));

        UserResponseDTO profile = userService.getProfile();

        assertThat(profile.email()).isEqualTo(TestDataFactory.TEST_EMAIL);
        assertThat(profile.fullName()).contains(TestDataFactory.TEST_FIRST_NAME);
    }

    @Test
    void getProfile_whenUserNotFound_throwsResourceNotFoundException() {
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────────────── changePassword ────────────────────────────

    @Test
    void changePassword_withCorrectOldPassword_updatesPassword() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TestDataFactory.TEST_PASSWORD_RAW, user.password())).thenReturn(true);
        when(passwordEncoder.encode("NewPass@1234!")).thenReturn("new-hashed-pw");

        userService.changePassword(new ResetCredentials(
                TestDataFactory.TEST_USERNAME,
                TestDataFactory.TEST_PASSWORD_RAW,
                "NewPass@1234!",
                "NewPass@1234!"));

        verify(userRepository).updatePassword(any(), eq("new-hashed-pw"));
    }

    @Test
    void changePassword_whenNewPasswordsMismatch_throwsUserException() {
        assertThatThrownBy(() -> userService.changePassword(new ResetCredentials(
                TestDataFactory.TEST_USERNAME,
                TestDataFactory.TEST_PASSWORD_RAW,
                "NewPass@1234!",
                "Different@456!")))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).updatePassword(any(), any());
    }

    @Test
    void changePassword_whenOldPasswordWrong_throwsAuthenticationException() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOld@1!", user.password())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(new ResetCredentials(
                TestDataFactory.TEST_USERNAME,
                "WrongOld@1!",
                "NewPass@1234!",
                "NewPass@1234!")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    // ──────────────────────────── verifyPhoneOtp ────────────────────────────

    @Test
    void verifyPhoneOtp_withCorrectOtp_marksPhoneVerified() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(otpService.verify(TestDataFactory.TEST_PHONE, "123456")).thenReturn(true);

        userService.verifyPhoneOtp("123456");

        verify(userRepository).markPhoneVerified(any());
    }

    @Test
    void verifyPhoneOtp_withWrongOtp_throwsAuthenticationException() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));
        when(otpService.verify(TestDataFactory.TEST_PHONE, "000000")).thenReturn(false);

        assertThatThrownBy(() -> userService.verifyPhoneOtp("000000"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }
}
