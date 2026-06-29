package com.immortals.auth.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.immortals.auth.service.event.NotificationEventPublisher;
import com.immortals.auth.service.util.TestRsaKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import org.junit.jupiter.api.BeforeEach;

import java.security.KeyPair;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base class for all integration tests.
 *
 * Boots the full Spring context against an H2 in-memory database.
 * Uses @MockitoBean (Spring Boot 4.x) to suppress real Kafka calls.
 * Injects RSA keys for JwtProvider via @DynamicPropertySource.
 * Each test is @Transactional so the database is rolled back after each test.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

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

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }
}
