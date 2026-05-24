package com.immortals.auth.service.security.jwt;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public class InvalidJwtToken implements JwtToken {

    public static final InvalidJwtToken INSTANCE = new InvalidJwtToken();

    private InvalidJwtToken() {
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public String username() {
        return null;
    }

    @Override
    public List<GrantedAuthority> authorities() {
        return List.of();
    }
}