package com.immortals.auth.service.security;

import com.immortals.auth.service.db.annotation.ReadOnly;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.repository.AuthRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthRepository authRepository;

    public UserDetailsServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @ReadOnly
    @Override
    public UserDetails loadUserByUsername(String userNameOrEmailOrPhone) throws UsernameNotFoundException {
        User user = authRepository.findUser(userNameOrEmailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + userNameOrEmailOrPhone));

        List<String> permissions = user.roles() != null
                ? user.roles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return UserPrincipal.create(
                user,
                user.roles() != null ? user.roles() : Collections.emptySet(),
                permissions
        );
    }

}

