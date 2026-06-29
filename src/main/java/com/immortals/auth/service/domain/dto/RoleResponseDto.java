package com.immortals.auth.service.domain.dto;

import java.util.Set;

public record RoleResponseDto(
        Long id,
        String name,
        String description,
        Set<String> permissions
) {
}
