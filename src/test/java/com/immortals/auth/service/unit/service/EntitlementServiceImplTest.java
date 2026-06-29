package com.immortals.auth.service.unit.service;

import com.immortals.auth.service.domain.dto.CreatePermissionDto;
import com.immortals.auth.service.domain.dto.CreateRoleRequest;
import com.immortals.auth.service.domain.dto.PermissionResponseDto;
import com.immortals.auth.service.domain.dto.RoleResponseDto;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.repository.PermissionsRepository;
import com.immortals.auth.service.repository.RoleRepository;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.service.exception.DuplicateResourceException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.auth.service.service.impl.EntitlementServiceImpl;
import com.immortals.auth.service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceImplTest {

    @Mock RoleRepository roleRepository;
    @Mock PermissionsRepository permissionsRepository;
    @Mock UserRepository userRepository;

    @InjectMocks
    EntitlementServiceImpl entitlementService;

    private void setupSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ──────────────────────────── createRole ────────────────────────────

    @Test
    void createRole_withUniqueName_savesAndReturnsDto() {
        Roles role = TestDataFactory.buildRole("TESTER");
        when(roleRepository.existsByRoleName("TESTER")).thenReturn(false);
        when(roleRepository.save(any(Roles.class))).thenReturn(role);

        RoleResponseDto dto = entitlementService.create(new CreateRoleRequest("TESTER", "Test role"));

        assertThat(dto.name()).isEqualTo("TESTER");
        verify(roleRepository).save(any(Roles.class));
    }

    @Test
    void createRole_whenNameAlreadyExists_throwsDuplicateResourceException() {
        when(roleRepository.existsByRoleName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> entitlementService.create(new CreateRoleRequest("ADMIN", "desc")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ADMIN");

        verify(roleRepository, never()).save(any());
    }

    // ──────────────────────────── getRoles ────────────────────────────

    @Test
    void getRoles_returnsAllRolesAsDtos() {
        Roles r1 = TestDataFactory.buildRole("USER");
        Roles r2 = TestDataFactory.buildRole("ADMIN");
        when(roleRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RoleResponseDto> roles = entitlementService.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(RoleResponseDto::name).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    // ──────────────────────────── deleteRole ────────────────────────────

    @Test
    void deleteRole_whenExists_deletesRole() {
        Roles role = TestDataFactory.buildRole("TESTER");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        entitlementService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRole_whenNotFound_throwsResourceNotFoundException() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entitlementService.deleteRole(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────────────── createPermission ────────────────────────────

    @Test
    void createPermission_withUniqueName_savesAndReturnsDto() {
        Permissions perm = TestDataFactory.buildPermission("READ_USERS");
        when(permissionsRepository.existsByName("READ_USERS")).thenReturn(false);
        when(permissionsRepository.save(any(Permissions.class))).thenReturn(perm);

        PermissionResponseDto dto = entitlementService.createPermission(
                new CreatePermissionDto("READ_USERS", "Read users"));

        assertThat(dto.name()).isEqualTo("READ_USERS");
    }

    @Test
    void createPermission_whenDuplicateName_throwsDuplicateResourceException() {
        when(permissionsRepository.existsByName("READ_USERS")).thenReturn(true);

        assertThatThrownBy(() -> entitlementService.createPermission(
                new CreatePermissionDto("READ_USERS", "desc")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(permissionsRepository, never()).save(any());
    }

    // ──────────────────────────── deletePermission ────────────────────────────

    @Test
    void deletePermission_whenExists_deletesPermission() {
        Permissions perm = TestDataFactory.buildPermission("WRITE_USERS");
        when(permissionsRepository.findById(1L)).thenReturn(Optional.of(perm));

        entitlementService.deletePermission(1L);

        verify(permissionsRepository).delete(perm);
    }

    @Test
    void deletePermission_whenNotFound_throwsResourceNotFoundException() {
        when(permissionsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entitlementService.deletePermission(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────────────── assignRolesToUser ────────────────────────────

    @Test
    void assignRolesToUser_whenUserDoesNotHaveRole_addsRoleAndSaves() {
        User user = TestDataFactory.buildUser();
        Roles role = TestDataFactory.buildRole("MANAGER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        entitlementService.assignRolesToUser(TestDataFactory.buildAssignRoleRequest(1L, 2L));

        verify(userRepository).save(user);
        assertThat(user.roles()).contains(role);
    }

    @Test
    void assignRolesToUser_whenUserAlreadyHasRole_throwsDuplicateResourceException() {
        User user = TestDataFactory.buildUser();
        Roles role = TestDataFactory.buildRole("MANAGER");
        user.roles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> entitlementService.assignRolesToUser(
                TestDataFactory.buildAssignRoleRequest(1L, 2L)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // ──────────────────────────── removeRoleFromUser ────────────────────────────

    @Test
    void removeRoleFromUser_whenUserHasRole_removesAndSaves() {
        User user = TestDataFactory.buildUser();
        Roles role = TestDataFactory.buildRole("MANAGER");
        user.roles().add(role);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        entitlementService.removeRoleFromUser(1L, 2L);

        verify(userRepository).save(user);
        assertThat(user.roles()).doesNotContain(role);
    }

    @Test
    void removeRoleFromUser_whenUserDoesNotHaveRole_throwsResourceNotFoundException() {
        User user = TestDataFactory.buildUser();
        Roles role = TestDataFactory.buildRole("MANAGER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> entitlementService.removeRoleFromUser(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────────────── assignPermissionsToRoles ────────────────────────────

    @Test
    void assignPermissionsToRoles_whenRoleDoesNotHavePermission_assignsAndSaves() {
        Roles role = TestDataFactory.buildRole("MANAGER");
        Permissions perm = TestDataFactory.buildPermission("READ_REPORTS");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionsRepository.findById(2L)).thenReturn(Optional.of(perm));

        entitlementService.assignPermissionsToRoles(TestDataFactory.buildAssignPermissionRequest(1L, 2L));

        verify(roleRepository).save(role);
        assertThat(role.getPermissions()).contains(perm);
    }

    // ──────────────────────────── getRolesForUsers ────────────────────────────

    @Test
    void getRolesForUsers_returnsCurrentUserRoles() {
        setupSecurityContext(TestDataFactory.TEST_USERNAME);
        User user = TestDataFactory.buildUser();
        Roles role = TestDataFactory.buildRole("USER");
        user.roles().add(role);
        when(userRepository.findByUserName(TestDataFactory.TEST_USERNAME)).thenReturn(Optional.of(user));

        List<RoleResponseDto> roles = entitlementService.getRolesForUsers();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).name()).isEqualTo("USER");
    }
}
