package com.immortals.auth.service.unit.security;

import com.immortals.auth.service.config.JwtKeyProperties;
import com.immortals.auth.service.security.UserPrincipal;
import com.immortals.auth.service.security.jwt.InvalidJwtToken;
import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.security.jwt.JwtToken;
import com.immortals.auth.service.security.jwt.ValidJwtToken;
import com.immortals.auth.service.util.TestRsaKeyUtil;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtProviderTest {

    private static JwtProvider jwtProvider;
    private static KeyPair keyPair;

    @BeforeAll
    static void setUpProvider() throws Exception {
        keyPair = TestRsaKeyUtil.generateKeyPair();

        String privatePem = TestRsaKeyUtil.toPrivatePem(keyPair.getPrivate());
        String publicPem  = TestRsaKeyUtil.toPublicPem(keyPair.getPublic());

        JwtKeyProperties props = new JwtKeyProperties(null, privatePem, null, publicPem);
        jwtProvider = new JwtProvider(props);

        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", 900_000);
        ReflectionTestUtils.setField(jwtProvider, "jwtIssuer", "http://localhost:8080");

        jwtProvider.initKeys();
    }

    private Authentication buildAuthentication(String username) {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getUsername()).thenReturn(username);
        when(principal.getUserId()).thenReturn(1L);
        when(principal.getEmail()).thenReturn(username + "@example.com");
        when(principal.getAuthorities()).thenReturn(Collections.emptyList());
        when(principal.getPermissions()).thenReturn(Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    @Test
    void generateAccessToken_returnsNonBlankSerializedJwt() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void generateRefreshToken_returnsNonBlankSerializedJwt() throws Exception {
        String token = jwtProvider.generateRefreshToken(buildAuthentication("testuser"), 604_800_000);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void validateToken_withValidToken_returnsTrue() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_withGarbageString_returnsFalse() {
        assertThat(jwtProvider.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void validateToken_withTamperedPayload_returnsFalse() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        String[] parts = token.split("\\.");
        String tampered = parts[0] + ".tampered_payload." + parts[2];
        assertThat(jwtProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void getUsernameFromToken_returnsSubjectMatchingPrincipalUsername() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("alice"));
        assertThat(jwtProvider.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    void getJti_returnsNonNullUniqueIdentifier() throws Exception {
        String t1 = jwtProvider.generateAccessToken(buildAuthentication("user1"));
        String t2 = jwtProvider.generateAccessToken(buildAuthentication("user1"));
        assertThat(jwtProvider.getJti(t1)).isNotNull();
        assertThat(jwtProvider.getJti(t1)).isNotEqualTo(jwtProvider.getJti(t2));
    }

    @Test
    void getExpiryEpochSecond_returnsPositiveValue() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        assertThat(jwtProvider.getExpiryEpochSecond(token)).isPositive();
    }

    @Test
    void getClaimsFromToken_containsExpectedClaims() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        JWTClaimsSet claims = jwtProvider.getClaimsFromToken(token);
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.getIssuer()).isEqualTo("http://localhost:8080");
        assertThat(claims.getJWTID()).isNotBlank();
    }

    @Test
    void parse_withValidToken_returnsValidJwtToken() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        JwtToken result = jwtProvider.parse(token);
        assertThat(result).isInstanceOf(ValidJwtToken.class);
        assertThat(result.username()).isEqualTo("testuser");
    }

    @Test
    void parse_withInvalidToken_returnsInvalidJwtToken() {
        JwtToken result = jwtProvider.parse("completely.invalid.token");
        assertThat(result).isSameAs(InvalidJwtToken.INSTANCE);
    }

    @Test
    void parse_withTamperedToken_returnsInvalidJwtToken() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        String[] parts = token.split("\\.");
        JwtToken result = jwtProvider.parse(parts[0] + "." + parts[1] + ".badsignature");
        assertThat(result).isSameAs(InvalidJwtToken.INSTANCE);
    }

    @Test
    void getPermissionsFromToken_returnsEmptyListWhenNoneSet() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        assertThat(jwtProvider.getPermissionsFromToken(token)).isEmpty();
    }

    @Test
    void getExpiryTimeFromToken_returnsPositiveDuration() throws Exception {
        String token = jwtProvider.generateAccessToken(buildAuthentication("testuser"));
        assertThat(jwtProvider.getExpiryTimeFromToken(token).toMillis()).isPositive();
    }
}