package com.immortals.auth.service.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(@JsonProperty(access = JsonProperty.Access.READ_ONLY) String username,
                               @JsonProperty(access = JsonProperty.Access.READ_ONLY) String accessToken,
                               @JsonProperty(access = JsonProperty.Access.READ_ONLY) String refreshToken,
                               @JsonProperty(access = JsonProperty.Access.READ_ONLY) String message) {
}
