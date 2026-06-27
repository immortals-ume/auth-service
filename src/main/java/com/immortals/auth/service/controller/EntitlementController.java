package com.immortals.auth.service.controller;

import com.immortals.auth.service.domain.dto.*;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.service.EntitlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/entitlements")
public class EntitlementController {

    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponseDto createRoles(
            @RequestBody @Valid Roles roles) {

        return entitlementService.create(roles);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<RoleResponseDto> getRolesForUser() {
        return entitlementService.getRolesForUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/assign-roles",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void assignRolesToUser(
            @RequestBody @Valid AssignRoleRequest request) {

        entitlementService.assignRolesToUser(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/assign-permissions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void assignPermissionsToRoles(
            @RequestBody @Valid AssignPermissionRequest request) {

        entitlementService.assignPermissionsToRoles(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/permissions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponseDto createPermission(
            @RequestBody @Valid CreatePermissionDto dto) {

        return entitlementService.createPermission(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            value = "/permissions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public List<PermissionResponseDto> getPermissions() {
        return entitlementService.getPermissions();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            value = "/roles",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public List<RoleResponseDto> getRoles() {
        return entitlementService.getRoles();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(
            @PathVariable Long roleId) {

        entitlementService.deleteRole(roleId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(
            @PathVariable Long permissionId) {

        entitlementService.deletePermission(permissionId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        entitlementService.removeRoleFromUser(userId, roleId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        entitlementService.removePermissionFromRole(roleId, permissionId);
    }
}