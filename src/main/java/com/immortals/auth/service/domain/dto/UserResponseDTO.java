package com.immortals.auth.service.domain.dto;

public record UserResponseDTO(
        Long id,
        String fullName,
        String email,
        String address,
        String phoneNumber
) {
}