package com.immortals.auth.service.security;

import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Unified principal that works for both form-login (UserDetails) and
 * OAuth2 social-login (OAuth2User), so the same JWT generation path
 * is used regardless of the authentication mechanism.
 */
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long userId;
    private final String userName;

    @JsonIgnore
    private final String password;

    private final String email;

    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    private final Collection<? extends GrantedAuthority> authorities;
    private final Collection<String> permissions;

    private Map<String, Object> attributes;

    private UserPrincipal(Long userId,
                          String userName,
                          String password,
                          String email,
                          Collection<? extends GrantedAuthority> authorities,
                          Collection<String> permissions,
                          boolean accountNonExpired,
                          boolean accountNonLocked,
                          boolean credentialsNonExpired,
                          boolean enabled) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
        this.permissions = permissions != null ? permissions : Collections.emptyList();
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    /** Form-login path: built from the local DB user record. */
    public static UserPrincipal create(User user, Collection<Roles> roles, Collection<String> permissions) {
        return new UserPrincipal(
                user.getId(),
                user.userName(),
                user.password(),
                user.email(),
                extractAuthorities(roles),
                permissions,
                Boolean.TRUE.equals(user.accountNonExpired()),
                Boolean.TRUE.equals(user.accountNonLocked()) && !Boolean.TRUE.equals(user.accountLocked()),
                Boolean.TRUE.equals(user.credentialsNonExpired()),
                Boolean.TRUE.equals(user.activeInd())
        );
    }

    /** OAuth2 path: same as create() but with provider attributes attached. */
    public static UserPrincipal createFromOAuth2(User user, Collection<Roles> roles,
                                                  Collection<String> permissions,
                                                  Map<String, Object> attributes) {
        UserPrincipal principal = create(user, roles, permissions);
        principal.attributes = attributes;
        return principal;
    }

    private static Collection<? extends GrantedAuthority> extractAuthorities(Collection<Roles> roles) {
        if (roles == null) return Collections.emptyList();
        return roles.stream()
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .toList();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    /** OAuth2User.getName() — required; we return the internal user ID string. */
    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserPrincipal that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Collection<String> getPermissions() {
        return permissions;
    }
}

