package com.immortals.auth.service.unit.security;

import com.immortals.auth.service.security.jwt.InvalidJwtToken;
import com.immortals.auth.service.security.jwt.ValidJwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenTest {

    @Test
    void validJwtToken_isValid_returnsTrue() {
        ValidJwtToken token = new ValidJwtToken("johndoe", List.of());
        assertThat(token.isValid()).isTrue();
    }

    @Test
    void validJwtToken_username_returnsUsername() {
        ValidJwtToken token = new ValidJwtToken("johndoe", List.of());
        assertThat(token.username()).isEqualTo("johndoe");
    }

    @Test
    void validJwtToken_authorities_returnsAuthorities() {
        var authority = new SimpleGrantedAuthority("ROLE_USER");
        ValidJwtToken token = new ValidJwtToken("johndoe", List.of(authority));
        assertThat(token.authorities()).containsExactly(authority);
    }

    @Test
    void invalidJwtToken_isValid_returnsFalse() {
        assertThat(InvalidJwtToken.INSTANCE.isValid()).isFalse();
    }

    @Test
    void invalidJwtToken_username_returnsNull() {
        assertThat(InvalidJwtToken.INSTANCE.username()).isNull();
    }

    @Test
    void invalidJwtToken_authorities_returnsEmptyList() {
        assertThat(InvalidJwtToken.INSTANCE.authorities()).isEmpty();
    }
}
