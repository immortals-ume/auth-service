package com.immortals.auth.service.security.service;

import com.immortals.auth.service.config.ServiceClientProperties;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Looks up service-to-service clients by client ID from ServiceClientProperties.
 * Used exclusively by the Basic-auth filter chain on /api/internal/** endpoints.
 * Each client entry stores a DelegatingPasswordEncoder-encoded secret (e.g. {bcrypt}...).
 */
@Service("serviceClientDetailsService")
public class ServiceClientDetailsService implements UserDetailsService {

    private final ServiceClientProperties properties;

    public ServiceClientDetailsService(ServiceClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public UserDetails loadUserByUsername(String clientId) throws UsernameNotFoundException {
        return properties.getClients().stream()
                .filter(c -> clientId.equals(c.getClientId()))
                .findFirst()
                .map(c -> User.builder()
                        .username(c.getClientId())
                        .password(c.getClientSecret())
                        .authorities(c.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Service client not found: " + clientId));
    }
}

