package com.immortals.auth.service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record RegisterRequestDTO(
        @NotBlank
        String firstName,
        String middleName,
        @NotBlank
        String lastName,
        @Size(min = 3, max = 16) @NotBlank
        String userName,
        @Email(message = "Email is not in correct format")
        @NotBlank
        String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$",
                message = "Password must be 8–64 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String password,
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$",
                message = "Password must be 8–64 characters and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String retypedPassword,
        @NotBlank
        String phoneCode,
        @Pattern(message = "Contact number invalid", regexp = "^(\\+91)?[6-9][0-9]{9}$")
        String contactNumber
) implements Serializable {}