package com.immortals.auth.service.security;

import com.immortals.auth.service.db.annotation.ReadOnly;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.repository.AuthRepository;
import com.immortals.auth.service.repository.RoleRepository;
import com.immortals.auth.service.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthRepository authRepository;

    @ReadOnly
    @Override
    public UserDetails loadUserByUsername(String userNameOrEmailOrPhone) throws UsernameNotFoundException {
        User user = authRepository.findUser(userNameOrEmailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username or email: " + userNameOrEmailOrPhone));
        return UserPrincipal.create(user,null,null);
    }

}
