package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.db.annotation.ReadOnly;
import com.immortals.auth.service.db.annotation.WriteOnly;
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
import com.immortals.auth.service.service.UserService;
import com.immortals.auth.service.service.exception.AuthenticationException;
import com.immortals.auth.service.service.exception.DuplicateResourceException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.exception.UserException;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserServiceImpl implements UserService {

    private static final ILogger log = LogManager.getInstance().getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpService otpService;
    private final NotificationEventPublisher eventPublisher;

    @Value("${auth.base-url:http://localhost:8080}")
    private String baseUrl;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenRepository verificationTokenRepository,
                           OtpService otpService,
                           NotificationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.otpService = otpService;
        this.eventPublisher = eventPublisher;
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public RegisterResponseDTO register(RegisterRequestDTO registerDto) {
        log.info("Registration request received for username={} email={}",
                registerDto.userName(),
                registerDto.email());

        if (!registerDto.password().equals(registerDto.retypedPassword())) {
            log.warn("Password mismatch for username={}", registerDto.userName());
            throw new UserException("Passwords do not match");
        }

        if (userRepository.existsByEmail(registerDto.email())) {
            log.warn("Registration failed. Email already exists: {}", registerDto.email());
            throw new DuplicateResourceException("Email already registered");
        }

        if (userRepository.existsByUserName(registerDto.userName())) {
            log.warn("Registration failed. Username already exists: {}", registerDto.userName());
            throw new DuplicateResourceException("Username already exists");
        }

        try {
            User user = User.builder()
                    .firstName(registerDto.firstName())
                    .middleName(registerDto.middleName())
                    .lastName(registerDto.lastName())
                    .userName(registerDto.userName())
                    .email(registerDto.email())
                    .phoneCode(registerDto.phoneCode())
                    .contactNumber(registerDto.contactNumber())
                    .password(passwordEncoder.encode(registerDto.password()))
                    .emailVerified(false)
                    .phoneNumberVerified(false)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .accountLocked(false)
                    .credentialsNonExpired(true)
                    .activeInd(true)
                    .build();

            User savedUser = userRepository.save(user);

            triggerEmailVerification(savedUser);

            log.info("User registered successfully. userId={} username={}",
                    savedUser.getId(),
                    savedUser.userName());

            return new RegisterResponseDTO(savedUser.getId(), savedUser.userName(), "Registration successful");

        } catch (DataAccessException e) {
            log.error("Database error while registering username={} email={}",
                    registerDto.userName(), registerDto.email(), e);
            throw new UserException("Unable to register user", e);
        }
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void sendEmailVerification() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.emailVerified())) {
            log.info("Email already verified for '{}'.", username);
            return;
        }

        triggerEmailVerification(user);
        log.info("Verification email event published for '{}'.", username);
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository
                .findByTokenAndType(token, VerificationToken.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        if (vt.isExpired() || vt.used()) {
            throw new AuthenticationException("Verification token has expired or already been used");
        }

        userRepository.markEmailVerified(vt.user().getId());
        vt.markUsed();
        verificationTokenRepository.save(vt);

        log.info("Email verified for user id={}.", vt.user().getId());
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void sendPhoneOtp() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.phoneNumberVerified())) {
            log.info("Phone already verified for '{}'.", username);
            return;
        }

        String otp = otpService.generate(user.contactNumber());
        eventPublisher.publishOtpRequested(user.contactNumber(), otp, OtpServiceImpl.OTP_TTL_SECONDS);
        log.info("OTP event published for user '{}'.", username);
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void verifyPhoneOtp(String otp) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!otpService.verify(user.contactNumber(), otp)) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        userRepository.markPhoneVerified(user.getId());
        log.info("Phone number verified for user '{}'.", username);
    }

    @ReadOnly
    @Override
    public UserResponseDTO getProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fullName = Stream.of(user.firstName(), user.middleName(), user.lastName())
                .filter(s -> s != null && !s.isBlank())
                .collect(java.util.stream.Collectors.joining(" "));

        String phoneNumber = (user.phoneCode() != null ? user.phoneCode() : "")
                + (user.contactNumber() != null ? user.contactNumber() : "");

        return new UserResponseDTO(user.getId(), fullName, user.email(), null, phoneNumber.isBlank() ? null : phoneNumber);
    }

    @WriteOnly
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void changePassword(ResetCredentials credentials) {
        if (!credentials.password().equals(credentials.reTypePassword())) {
            throw new UserException("Passwords do not match");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(credentials.oldPassword(), user.password())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        userRepository.updatePassword(user.getId(), passwordEncoder.encode(credentials.password()));
        log.info("Password changed for user '{}'.", username);
    }

    private void triggerEmailVerification(User user) {
        verificationTokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        String tokenValue = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(tokenValue)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build();
        verificationTokenRepository.save(vt);

        String link = baseUrl + "/api/v1/user/verify-email/confirm?token=" + tokenValue;
        eventPublisher.publishEmailVerification(user.email(), link);
    }
}

