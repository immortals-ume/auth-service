package com.immortals.auth.service.security.jwt;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record ValidJwtToken(String username, List<GrantedAuthority> authorities) implements JwtToken {

    @Override
    public boolean isValid() {
        return true;
    }
}