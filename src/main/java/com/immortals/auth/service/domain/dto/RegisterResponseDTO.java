package com.immortals.auth.service.domain.dto;

public record RegisterResponseDTO(
        Long userId,
        String username,
        String message
) {
}

