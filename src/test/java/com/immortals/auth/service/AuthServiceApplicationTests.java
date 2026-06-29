package com.immortals.auth.service;

import com.immortals.auth.service.event.NotificationEventPublisher;
import com.immortals.auth.service.util.TestRsaKeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.KeyPair;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    private static KeyPair testKeyPair;

    @DynamicPropertySource
    static void jwtKeyProperties(DynamicPropertyRegistry registry) throws Exception {
        if (testKeyPair == null) {
            testKeyPair = TestRsaKeyUtil.generateKeyPair();
        }
        registry.add("jwt.key.private-key", () -> TestRsaKeyUtil.toPrivatePem(testKeyPair.getPrivate()));
        registry.add("jwt.key.public-key",  () -> TestRsaKeyUtil.toPublicPem(testKeyPair.getPublic()));
        registry.add("jwt.key.private-key-path", () -> "");
        registry.add("jwt.key.public-key-path",  () -> "");
    }

    @MockitoBean
    NotificationEventPublisher notificationEventPublisher;

    @Test
    void contextLoads() {
    }
}
