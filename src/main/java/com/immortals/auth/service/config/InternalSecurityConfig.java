package com.immortals.auth.service.config;

import com.immortals.auth.service.security.service.ServiceClientDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Separate security filter chain for service-to-service (S2S) calls.
 * Matches /api/internal/** only — evaluated before the JWT chain (this is @Order(1)).
 *
 * Clients authenticate with HTTP Basic using their client-id / client-secret.
 * Secrets in ServiceClientProperties use DelegatingPasswordEncoder prefixes:
 *   {noop}   — plaintext (dev only)
 *   {bcrypt} — BCrypt-hashed (production)
 *
 * Example curl:
 *   curl -u catalog-service:catalog-dev-secret https://auth-service/api/internal/validate
 */
@Configuration
@Order(1)
public class InternalSecurityConfig {

    private final ServiceClientDetailsService serviceClientDetailsService;

    public InternalSecurityConfig(ServiceClientDetailsService serviceClientDetailsService) {
        this.serviceClientDetailsService = serviceClientDetailsService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain internalSecurityFilterChain(HttpSecurity http)  {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(serviceClientDetailsService);
        provider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());

        http
            .securityMatcher("/api/internal/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(provider)
            .httpBasic(basic -> basic.realmName("shopwire-internal"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/internal/**").hasRole("SERVICE")
                .anyRequest().denyAll()
            );

        return http.build();
    }
}

