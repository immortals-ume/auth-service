package com.immortals.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Resolving order in JwtProvider: privateKeyPath (file) wins over privateKey (inline content).
 * Dev: set jwt.key.private-key-path / jwt.key.public-key-path to a local .pem file path.
 * Prod: Vault injects jwt.key.private-key and jwt.key.public-key as PEM content strings.
 */
@ConfigurationProperties(prefix = "jwt.key")
public record JwtKeyProperties(
        String privateKeyPath,
        String privateKey,
        String publicKeyPath,
        String publicKey
) {}

