package com.immortals.auth.service.security.jwt;

import com.immortals.auth.service.config.JwtKeyProperties;
import com.immortals.auth.service.security.UserPrincipal;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final ILogger log = LogManager.getInstance().getLogger(JwtProvider.class);

    private final JwtKeyProperties keyProperties;

    @Value("${auth.access-token-expiry-ms}")
    private int jwtExpirationMs;

    @Value("${auth.jwt-issuer}")
    private String jwtIssuer;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public JwtProvider(JwtKeyProperties keyProperties) {
        this.keyProperties = keyProperties;
    }

    @PostConstruct
    public void initKeys() throws IOException, GeneralSecurityException {
        log.info("Initializing JWT keys...");
        this.privateKey = parsePrivateKey(
                resolveKeyContent(keyProperties.privateKeyPath(), keyProperties.privateKey(), "private"));
        this.publicKey = parsePublicKey(
                resolveKeyContent(keyProperties.publicKeyPath(), keyProperties.publicKey(), "public"));
        log.info("JWT keys initialized successfully.");
    }

    private String resolveKeyContent(String path, String inline, String label) throws IOException {
        if (path != null && !path.isBlank()) {
            Path keyPath = Path.of(path);
            if (!Files.exists(keyPath)) {
                throw new IllegalStateException(
                        "JWT " + label + " key file not found: " + keyPath.toAbsolutePath()
                        + " — set jwt.key." + label + "-key-path (file) or jwt.key."
                        + label + "-key (PEM content).");
            }
            log.info("Loading JWT {} key from file: {}", label, keyPath.toAbsolutePath());
            return Files.readString(keyPath);
        }
        if (inline != null && !inline.isBlank()) {
            log.info("Loading JWT {} key from configuration (inline).", label);
            return inline;
        }
        throw new IllegalStateException(
                "JWT " + label + " key not configured. "
                + "Set jwt.key." + label + "-key-path (file path) "
                + "or jwt.key." + label + "-key (PEM content).");
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
        byte[] decoded = Base64.getDecoder().decode(stripPemHeaders(pem));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey parsePublicKey(String pem) throws GeneralSecurityException {
        byte[] decoded = Base64.getDecoder().decode(stripPemHeaders(pem));
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    private static String stripPemHeaders(String pem) {
        return pem.replaceAll("-{5}(BEGIN|END)[^-]*-{5}", "").replaceAll("\\s+", "");
    }

    public String generateAccessToken(Authentication authentication) throws JOSEException {
        log.info("Generating access token for user: {}", authentication.getName());
        return buildToken(authentication, jwtExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication, int refreshExpiryMs)
            throws JOSEException {
        log.info("Generating refresh token for user: {}", authentication.getName());
        return buildToken(authentication, refreshExpiryMs);
    }

    private String buildToken(Authentication authentication, int expiryMs) throws JOSEException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expiryMs);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(principal.getUsername())
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", principal.getUserId())
                .claim("email", principal.getEmail())
                .claim("roles", principal.getAuthorities())
                .claim("permissions", principal.getPermissions())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .issuer(jwtIssuer)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(this.privateKey));
        log.debug("Token built for '{}' (exp: {})", principal.getUsername(), expiresAt);
        return jwt.serialize();
    }

    /**
     * Single-parse entry point for JwtAuthenticationFilter.
     * Never throws — returns InvalidJwtToken.INSTANCE on any failure.
     */
    public JwtToken parse(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(this.publicKey))
                    || new Date().after(jwt.getJWTClaimsSet().getExpirationTime())) {
                return InvalidJwtToken.INSTANCE;
            }
            return new ValidJwtToken(jwt.getJWTClaimsSet().getSubject(), List.of());
        } catch (Exception e) {
            log.error("JWT parse failed: {}", e.getMessage());
            return InvalidJwtToken.INSTANCE;
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean valid = jwt.verify(new RSASSAVerifier(this.publicKey))
                    && new Date().before(jwt.getJWTClaimsSet().getExpirationTime());
            log.debug("Token validation result: {}", valid);
            return valid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) throws ParseException {
        return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    }

    public JWTClaimsSet getClaimsFromToken(String token) throws ParseException {
        return SignedJWT.parse(token).getJWTClaimsSet();
    }

    public List<String> getPermissionsFromToken(String token) throws ParseException {
        Object perms = getClaimsFromToken(token).getClaim("permissions");
        return perms != null ? (List<String>) perms : List.of();
    }

    public Duration getExpiryTimeFromToken(String token) throws ParseException {
        long ttl = getClaimsFromToken(token).getExpirationTime().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(ttl, 0));
    }

    /** Returns the JTI used as the blacklist key on logout. */
    public String getJti(String token) {
        try {
            return getClaimsFromToken(token).getJWTID();
        } catch (ParseException e) {
            log.error("Failed to extract jti: {}", e.getMessage());
            return null;
        }
    }

    /** Returns the expiration epoch-second used to set the blacklist entry TTL. */
    public long getExpiryEpochSecond(String token) {
        try {
            return getClaimsFromToken(token).getExpirationTime().toInstant().getEpochSecond();
        } catch (ParseException e) {
            log.error("Failed to extract expiry epoch: {}", e.getMessage());
            return 0L;
        }
    }
}

