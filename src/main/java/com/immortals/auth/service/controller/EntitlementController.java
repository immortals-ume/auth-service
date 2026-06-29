package com.immortals.auth.service.controller;

import com.immortals.auth.service.audit.AuditSeverity;
import com.immortals.auth.service.audit.AuditType;
import com.immortals.auth.service.audit.annotation.AuditAction;
import com.immortals.auth.service.domain.dto.*;
import com.immortals.auth.service.service.EntitlementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/entitlements")
public class EntitlementController {

    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @AuditAction(action = "ROLE_CREATED", type = AuditType.ENTITY_CHANGE, resourceType = "ROLE")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/roles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponseDto createRole(@RequestBody @Valid CreateRoleRequest request) {
        return entitlementService.create(request);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleResponseDto> getRolesForUser() {
        return entitlementService.getRolesForUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RoleResponseDto> getRoles() {
        return entitlementService.getRoles();
    }

    @AuditAction(action = "ROLE_DELETED", type = AuditType.ENTITY_CHANGE, severity = AuditSeverity.HIGH, resourceType = "ROLE")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable @Positive Long roleId) {
        entitlementService.deleteRole(roleId);
    }

    @AuditAction(action = "ROLE_ASSIGNED", type = AuditType.ENTITY_CHANGE, severity = AuditSeverity.HIGH, resourceType = "USER")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/assign-roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignRolesToUser(@RequestBody @Valid AssignRoleRequest request) {
        entitlementService.assignRolesToUser(request);
    }

    @AuditAction(action = "ROLE_REMOVED", type = AuditType.ENTITY_CHANGE, severity = AuditSeverity.HIGH, resourceType = "USER")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRoleFromUser(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long roleId) {
        entitlementService.removeRoleFromUser(userId, roleId);
    }

    @AuditAction(action = "PERMISSION_CREATED", type = AuditType.ENTITY_CHANGE, resourceType = "PERMISSION")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/permissions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponseDto createPermission(@RequestBody @Valid CreatePermissionDto dto) {
        return entitlementService.createPermission(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PermissionResponseDto> getPermissions() {
        return entitlementService.getPermissions();
    }

    @AuditAction(action = "PERMISSION_DELETED", type = AuditType.ENTITY_CHANGE, severity = AuditSeverity.HIGH, resourceType = "PERMISSION")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable @Positive Long permissionId) {
        entitlementService.deletePermission(permissionId);
    }

    @AuditAction(action = "PERMISSION_ASSIGNED", type = AuditType.ENTITY_CHANGE, resourceType = "ROLE")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/assign-permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignPermissionsToRoles(@RequestBody @Valid AssignPermissionRequest request) {
        entitlementService.assignPermissionsToRoles(request);
    }

    @AuditAction(action = "PERMISSION_REMOVED", type = AuditType.ENTITY_CHANGE, resourceType = "ROLE")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePermissionFromRole(
            @PathVariable @Positive Long roleId,
            @PathVariable @Positive Long permissionId) {
        entitlementService.removePermissionFromRole(roleId, permissionId);
    }
}

