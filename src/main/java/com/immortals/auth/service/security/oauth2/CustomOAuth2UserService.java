package com.immortals.auth.service.security.oauth2;

import com.immortals.auth.service.db.annotation.WriteOnly;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.domain.enums.AuthProvider;
import com.immortals.auth.service.repository.RoleRepository;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.security.UserPrincipal;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Plugs into Spring Security's OAuth2 login flow.
 * Loads the provider user info, then finds-or-creates a local User record,
 * and returns a UserPrincipal that works for both UserDetails and OAuth2User contracts.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final ILogger log = LogManager.getInstance().getLogger(CustomOAuth2UserService.class);
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @WriteOnly
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo info;
        try {
            info = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"), e.getMessage(), e);
        }

        if (info.getEmail() == null || info.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"),
                    "Email not returned by " + registrationId + ". Ensure email scope is requested.", null);
        }

        AuthProvider provider = OAuth2UserInfoFactory.toAuthProvider(registrationId);
        User user = userRepository.findByProviderIdAndAuthProvider(info.getProviderId(), provider)
                .map(existing -> refreshOAuth2User(existing, info))
                .orElseGet(() -> userRepository.findByEmail(info.getEmail())
                        .map(linked -> linkOAuth2ToExistingAccount(linked, info, provider))
                        .orElseGet(() -> createOAuth2User(info, provider)));

        Set<Roles> roles = user.roles() != null ? user.roles() : Set.of();
        Set<String> permissions = roles.stream()
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(Permissions::getName)
                .collect(java.util.stream.Collectors.toSet());

        log.info("OAuth2 login via {} for user '{}'", registrationId, user.email());
        return UserPrincipal.createFromOAuth2(user, roles, permissions, oAuth2User.getAttributes());
    }

    private User createOAuth2User(OAuth2UserInfo info, AuthProvider provider) {
        String[] nameParts = splitName(info.getName());
        Roles defaultRole = roleRepository.findByRoleName(DEFAULT_ROLE).orElse(null);

        User user = User.builder()
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .userName(deriveUsername(info.getEmail()))
                .email(info.getEmail())
                .emailVerified(true)
                .authProvider(provider)
                .providerId(info.getProviderId())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .accountLocked(false)
                .credentialsNonExpired(true)
                .activeInd(true)
                .phoneNumberVerified(false)
                .roles(defaultRole != null ? Set.of(defaultRole) : Set.of())
                .build();

        return userRepository.save(user);
    }

    private User refreshOAuth2User(User existing, OAuth2UserInfo info) {
        return existing;
    }

    private User linkOAuth2ToExistingAccount(User existing, OAuth2UserInfo info, AuthProvider provider) {
        existing.setAuthProvider(provider);
        existing.setProviderId(info.getProviderId());
        return userRepository.save(existing);
    }

    private static String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"User", "Unknown"};
        int space = fullName.indexOf(' ');
        if (space < 0) return new String[]{fullName, fullName};
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }

    private static String deriveUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
        return base.length() > 16 ? base.substring(0, 16) : base;
    }
}

