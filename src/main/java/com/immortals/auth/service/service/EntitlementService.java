package com.immortals.auth.service.service;

import com.immortals.auth.service.domain.dto.*;

import java.util.List;

public interface EntitlementService {
    RoleResponseDto create(CreateRoleRequest request);

    List<RoleResponseDto> getRolesForUsers();

    void assignRolesToUser(AssignRoleRequest request);

    void assignPermissionsToRoles(AssignPermissionRequest request);

    void deleteRole(Long roleId);

    List<RoleResponseDto> getRoles();

    PermissionResponseDto createPermission(CreatePermissionDto dto);

    List<PermissionResponseDto> getPermissions();

    void deletePermission(Long permissionId);

    void removeRoleFromUser(Long userId, Long roleId);

    void removePermissionFromRole(Long roleId, Long permissionId);
}

