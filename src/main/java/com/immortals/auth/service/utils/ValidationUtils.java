package com.immortals.auth.service.utils;

import jakarta.validation.ValidationException;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.immortals.auth.service.constant.AuthConstants.*;

public final class ValidationUtils {

    private static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX_INDIA);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private ValidationUtils() {
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static Optional<String> validateMobile(String mobile) {
        return isValidMobile(mobile) ? Optional.empty() : Optional.of("Invalid mobile number");
    }

    public static Optional<String> validatePassword(String password) {
        return isValidPassword(password) ? Optional.empty() : Optional.of("Invalid password");
    }

    public static Optional<String> validateEmail(String email) {
        return isValidEmail(email) ? Optional.empty() : Optional.of("Invalid email");
    }

    public static void requireValidMobile(String mobile) {
        if (!isValidMobile(mobile)) {
            throw new ValidationException("Invalid mobile number");
        }
    }

    public static void requireValidPassword(String password) {
        if (!isValidPassword(password)) {
            throw new ValidationException("Invalid password");
        }
    }

    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email");
        }
    }
}
