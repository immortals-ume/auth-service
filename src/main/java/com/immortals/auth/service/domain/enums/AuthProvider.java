package com.immortals.auth.service.domain.enums;

public enum AuthProvider {
    FORM_LOGIN("formLogin"),
    SERVICE2SERVICE("service2service"),
    GMAIL("gmail"),
    GITHUB("github");

    private final String provider;

    AuthProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
