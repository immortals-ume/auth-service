package com.immortals.auth.service.security.jwt;

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
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final ILogger log = LogManager.getInstance().getLogger(JwtProvider.class);

    @Value("${jwt-private-key}")
    private String privateKeyPem;

    @Value("${jwt-public-key}")
    private String publicKeyPem;

    @Value("${auth.access-token-expiry-ms}")
    private int jwtExpirationMs;

    @Value("${auth.jwt-issuer}")
    private String jwtIssuer;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void initKeys() throws Exception {
        log.info("Initializing JWT keys...");
        this.privateKey = loadPrivateKeyFromPem(privateKeyPem);
        this.publicKey = loadPublicKeyFromPem(publicKeyPem);
        log.info("JWT keys successfully initialized.");
    }

    private RSAPrivateKey loadPrivateKeyFromPem(String pem)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.decodeBase64(key);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey loadPublicKeyFromPem(String pem)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.decodeBase64(key);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    public String generateAccessToken(Authentication authentication) throws JOSEException {
        log.info("Generating access token for user: {}", authentication.getName());
        return buildToken(authentication, jwtExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication, Integer refreshExpiryMs)
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
                .jwtID(UUID.randomUUID().toString())      // required for blacklisting
                .claim("userId", principal.getUserId())
                .claim("email", principal.getEmail())
                .claim("roles", principal.getAuthorities())
                .claim("permissions", principal.getPermissions())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .issuer(jwtIssuer)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(this.privateKey));      // fix: use instance field

        log.debug("Token built for '{}' (exp: {})", principal.getUsername(), expiresAt);
        return jwt.serialize();
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean valid = jwt.verify(new RSASSAVerifier(this.publicKey))  // fix: use instance field
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
        long ttl = getClaimsFromToken(token).getExpirationTime().getTime()
                - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(ttl, 0));
    }

    /**
     * Used by TokenBlacklistService on logout.
     */
    public String getJti(String token) {
        try {
            return getClaimsFromToken(token).getJWTID();
        } catch (ParseException e) {
            log.error("Failed to extract jti: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Used by TokenBlacklistService to set the blacklist entry TTL.
     */
    public long getExpiryEpochSecond(String token) {
        try {
            return getClaimsFromToken(token)
                    .getExpirationTime()
                    .toInstant()
                    .getEpochSecond();
        } catch (ParseException e) {
            log.error("Failed to extract expiry epoch: {}", e.getMessage());
            return 0L;
        }
    }
}