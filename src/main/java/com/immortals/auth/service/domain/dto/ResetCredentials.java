package com.immortals.auth.service.domain.dto;

public record ResetCredentials(String userName,
                               String password,
                               String reTypePassword) {
}
