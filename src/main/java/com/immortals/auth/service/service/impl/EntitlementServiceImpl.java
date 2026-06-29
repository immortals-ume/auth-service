package com.immortals.auth.service.service.impl;

import com.immortals.auth.service.db.annotation.ReadOnly;
import com.immortals.auth.service.db.annotation.WriteOnly;
import com.immortals.auth.service.domain.dto.*;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.repository.PermissionsRepository;
import com.immortals.auth.service.repository.RoleRepository;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.service.EntitlementService;
import com.immortals.auth.service.service.exception.DuplicateResourceException;
import com.immortals.auth.service.service.exception.ResourceNotFoundException;
import com.immortals.logging.api.ILogger;
import com.immortals.logging.core.LogManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EntitlementServiceImpl implements EntitlementService {

    private static final ILogger log = LogManager.getInstance().getLogger(EntitlementServiceImpl.class);

    private final RoleRepository rolesRepository;
    private final PermissionsRepository permissionsRepository;
    private final UserRepository usersRepository;

    public EntitlementServiceImpl(RoleRepository rolesRepository, PermissionsRepository permissionsRepository, UserRepository usersRepository) {
        this.rolesRepository = rolesRepository;
        this.permissionsRepository = permissionsRepository;
        this.usersRepository = usersRepository;
    }

    @WriteOnly
    @Override
    @Transactional
    public RoleResponseDto create(CreateRoleRequest request) {
        if (rolesRepository.existsByRoleName(request.roleName())) {
            throw new DuplicateResourceException(
                    "Role already exists: " + request.roleName());
        }
        Roles role = Roles.builder()
                .roleName(request.roleName())
                .description(request.description())
                .activeInd(true)
                .build();
        Roles saved = rolesRepository.save(role);
        log.info("Role created: {}", saved.getRoleName());
        return new RoleResponseDto(saved.getId(), saved.getRoleName(), saved.getDescription(), Set.of());
    }

    @ReadOnly
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getRoles() {
        return rolesRepository.findAll()
                .stream()
                .map(this::toRoleDto)
                .collect(Collectors.toList());
    }

    @WriteOnly
    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Roles role = findRoleOrThrow(roleId);
        role.getUsers().forEach(user -> user.roles().remove(role));
        role.getUsers().clear();

        rolesRepository.delete(role);
        log.info("Role deleted: id={}", roleId);
    }

    @WriteOnly
    @Override
    @Transactional
    public PermissionResponseDto createPermission(CreatePermissionDto dto) {
        if (permissionsRepository.existsByName(dto.name())) {
            throw new DuplicateResourceException(
                    "Permission already exists: " + dto.name());
        }
        Permissions permission = Permissions.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
        Permissions saved = permissionsRepository.save(permission);
        log.info("Permission created: {}", saved.getName());
        return toPermissionDto(saved);
    }

    @ReadOnly
    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDto> getPermissions() {
        return permissionsRepository.findAll()
                .stream()
                .map(this::toPermissionDto)
                .collect(Collectors.toList());
    }

    @WriteOnly
    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        Permissions permission = findPermissionOrThrow(permissionId);

        permission.getRoles().forEach(role -> role.getPermissions().remove(permission));
        permission.getRoles().clear();

        permissionsRepository.delete(permission);
        log.info("Permission deleted: id={}", permissionId);
    }

    @ReadOnly
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> getRolesForUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = usersRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        return user.roles().stream()
                .map(this::toRoleDto)
                .collect(Collectors.toList());
    }

    @WriteOnly
    @Override
    @Transactional
    public void assignRolesToUser(AssignRoleRequest request) {
        User user = findUserOrThrow(request.userId());
        Roles role = findRoleOrThrow(request.roleId());

        if (user.roles().contains(role)) {
            throw new DuplicateResourceException(
                    "User already has role: " + role.getRoleName());
        }
        user.roles().add(role);
        usersRepository.save(user);
        log.info("Role '{}' assigned to user id={}", role.getRoleName(), user.getId());
    }

    @WriteOnly
    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = findUserOrThrow(userId);
        Roles role = findRoleOrThrow(roleId);

        if (!user.roles().remove(role)) {
            throw new ResourceNotFoundException(
                    "User does not have role: " + role.getRoleName());
        }
        usersRepository.save(user);
        log.info("Role '{}' removed from user id={}", role.getRoleName(), userId);
    }

    @WriteOnly
    @Override
    @Transactional
    public void assignPermissionsToRoles(AssignPermissionRequest request) {
        Roles role = findRoleOrThrow(request.roleId());
        Permissions permission = findPermissionOrThrow(request.permissionId());

        if (role.getPermissions().contains(permission)) {
            throw new DuplicateResourceException(
                    "Role already has permission: " + permission.getName());
        }
        role.getPermissions().add(permission);
        rolesRepository.save(role);
        log.info("Permission '{}' assigned to role '{}'",
                permission.getName(), role.getRoleName());
    }

    @WriteOnly
    @Override
    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        Roles role = findRoleOrThrow(roleId);
        Permissions permission = findPermissionOrThrow(permissionId);

        if (!role.getPermissions().remove(permission)) {
            throw new ResourceNotFoundException(
                    "Role does not have permission: " + permission.getName());
        }
        rolesRepository.save(role);
        log.info("Permission '{}' removed from role '{}'",
                permission.getName(), role.getRoleName());
    }

    private Roles findRoleOrThrow(Long id) {
        return rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: id=" + id));
    }

    private Permissions findPermissionOrThrow(Long id) {
        return permissionsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: id=" + id));
    }

    private User findUserOrThrow(Long id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: id=" + id));
    }

    private RoleResponseDto toRoleDto(Roles role) {
        return new RoleResponseDto(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(Permissions::getName)
                        .collect(Collectors.toSet())
        );
    }

    private PermissionResponseDto toPermissionDto(Permissions p) {
        return new PermissionResponseDto(p.getId(), p.getName(), p.getDescription());
    }
}

