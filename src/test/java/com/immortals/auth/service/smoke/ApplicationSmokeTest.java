package com.immortals.auth.service.smoke;

import com.immortals.auth.service.controller.AuthController;
import com.immortals.auth.service.controller.EntitlementController;
import com.immortals.auth.service.controller.UserController;
import com.immortals.auth.service.event.NotificationEventPublisher;
import com.immortals.auth.service.security.UserDetailsServiceImpl;
import com.immortals.auth.service.security.jwt.JwtProvider;
import com.immortals.auth.service.service.AuthService;
import com.immortals.auth.service.service.EntitlementService;
import com.immortals.auth.service.service.UserService;
import com.immortals.auth.service.util.TestRsaKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke tests — verify the application context starts and key HTTP rules are enforced.
 */
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
class ApplicationSmokeTest {

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

    @Autowired ApplicationContext applicationContext;
    @Autowired WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // ──────────────────────── Spring context ────────────────────────

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void authController_beanIsPresent() {
        assertThat(applicationContext.getBean(AuthController.class)).isNotNull();
    }

    @Test
    void userController_beanIsPresent() {
        assertThat(applicationContext.getBean(UserController.class)).isNotNull();
    }

    @Test
    void entitlementController_beanIsPresent() {
        assertThat(applicationContext.getBean(EntitlementController.class)).isNotNull();
    }

    @Test
    void authService_beanIsPresent() {
        assertThat(applicationContext.getBean(AuthService.class)).isNotNull();
    }

    @Test
    void userService_beanIsPresent() {
        assertThat(applicationContext.getBean(UserService.class)).isNotNull();
    }

    @Test
    void entitlementService_beanIsPresent() {
        assertThat(applicationContext.getBean(EntitlementService.class)).isNotNull();
    }

    @Test
    void jwtProvider_beanIsPresent() {
        assertThat(applicationContext.getBean(JwtProvider.class)).isNotNull();
    }

    @Test
    void userDetailsService_beanIsPresent() {
        assertThat(applicationContext.getBean(UserDetailsServiceImpl.class)).isNotNull();
    }

    @Test
    void authenticationManager_beanIsPresent() {
        assertThat(applicationContext.getBean(AuthenticationManager.class)).isNotNull();
    }

    // ──────────────────────── Public endpoints ────────────────────────

    @Test
    void loginEndpoint_acceptsPost_returnsNotMethodAllowedForGet() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void registerEndpoint_acceptsPost_returnsMethodNotAllowedForGet() throws Exception {
        mockMvc.perform(get("/api/v1/user/register"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ──────────────────────── Protected endpoints require auth ────────────────────────

    @Test
    void profileEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rolesEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/entitlements/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEndpoint_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist"))
                .andExpect(status().isUnauthorized());
    }
}
