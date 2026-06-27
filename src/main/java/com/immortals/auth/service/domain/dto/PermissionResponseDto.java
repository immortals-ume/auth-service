package com.immortals.auth.service.domain.dto;


public record PermissionResponseDto(
        Long id,
        String name,
        String description
) {
}