package com.immortals.auth.service.config;

import com.immortals.auth.service.config.CorsProperties;
import com.immortals.auth.service.security.filters.JwtAuthenticationFilter;
import com.immortals.auth.service.security.oauth2.CustomOAuth2UserService;
import com.immortals.auth.service.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.immortals.auth.service.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.immortals.auth.service.security.provider.AuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.immortals.auth.service.constant.AuthConstants.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(2)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthEntryPoint unauthorizedHandler;
    private final UserDetailsService userDetailsService;
    private final CorsProperties corsProperties;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          AuthEntryPoint unauthorizedHandler,
                          UserDetailsService userDetailsService,
                          CorsProperties corsProperties,
                          CustomOAuth2UserService oAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler,
                          OAuth2AuthenticationFailureHandler oAuth2FailureHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.userDetailsService = userDetailsService;
        this.corsProperties = corsProperties;
        this.oAuth2UserService = oAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean("passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setMaxAge(corsProperties.getMaxAge() != null ? corsProperties.getMaxAge() : MAX_AGE_CORS_SECS);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // This chain handles /api/** and /oauth2/** — internal S2S is handled by InternalSecurityConfig
            .securityMatcher("/api/**", "/oauth2/**", "/login/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            // OAuth2 flows need session for the authorization code state parameter
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_RESOURCE_PATHS).permitAll()
                    .requestMatchers(ANONYMOUS_API_PATHS).permitAll()
                    .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                    .anyRequest().authenticated()
            )
            // ── Form login (email + password → JWT cookies) ──────────────────
            // The actual credential check is in AuthController → AuthServiceImpl → DaoAuthenticationProvider.
            // Spring Security's formLogin() is intentionally disabled to keep the JSON API contract.
            // ── OAuth2 Social Login ─────────────────────────────────────────
            .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(info -> info.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(
                            "default-src 'self'; " +
                                    "script-src 'self' https://cdn.example.com; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data:; " +
                                    "object-src 'none'; " +
                                    "frame-ancestors 'none';"
                    ))
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .httpStrictTransportSecurity(hsts -> hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(MAX_AGE_HSTS_SECS)
                            .preload(true)
                    )
            ).redirectToHttps(channel -> channel
                    .requestMatchers(request ->
                            "http".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))
                    )
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}

