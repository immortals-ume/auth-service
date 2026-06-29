package com.immortals.auth.service.unit.security;

import com.immortals.auth.service.config.ServiceClientProperties;
import com.immortals.auth.service.security.service.ServiceClientDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceClientDetailsServiceTest {

    @Mock ServiceClientProperties properties;
    @InjectMocks ServiceClientDetailsService service;

    private ServiceClientProperties.ClientEntry entry;

    @BeforeEach
    void setUp() {
        entry = new ServiceClientProperties.ClientEntry();
        entry.setClientId("catalog-service");
        entry.setClientSecret("{noop}catalog-secret");
        entry.setRoles(List.of("ROLE_SERVICE"));
    }

    @Test
    void loadUserByUsername_whenClientExists_returnsUserDetails() {
        when(properties.getClients()).thenReturn(List.of(entry));

        UserDetails ud = service.loadUserByUsername("catalog-service");

        assertThat(ud.getUsername()).isEqualTo("catalog-service");
        assertThat(ud.getPassword()).isEqualTo("{noop}catalog-secret");
        assertThat(ud.getAuthorities()).extracting("authority").containsExactly("ROLE_SERVICE");
    }

    @Test
    void loadUserByUsername_whenClientNotFound_throwsUsernameNotFoundException() {
        when(properties.getClients()).thenReturn(List.of(entry));

        assertThatThrownBy(() -> service.loadUserByUsername("unknown-client"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown-client");
    }

    @Test
    void loadUserByUsername_whenNoClientsConfigured_throwsUsernameNotFoundException() {
        when(properties.getClients()).thenReturn(List.of());

        assertThatThrownBy(() -> service.loadUserByUsername("any-client"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_withMultipleRoles_returnsAllAuthorities() {
        entry.setRoles(List.of("ROLE_SERVICE", "ROLE_INTERNAL"));
        when(properties.getClients()).thenReturn(List.of(entry));

        UserDetails ud = service.loadUserByUsername("catalog-service");

        assertThat(ud.getAuthorities()).hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_SERVICE", "ROLE_INTERNAL");
    }
}
