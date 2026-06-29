package com.immortals.auth.service.integration;

import com.immortals.auth.service.domain.dto.AssignPermissionRequest;
import com.immortals.auth.service.domain.dto.AssignRoleRequest;
import com.immortals.auth.service.domain.dto.CreatePermissionDto;
import com.immortals.auth.service.domain.dto.CreateRoleRequest;
import com.immortals.auth.service.domain.entity.Permissions;
import com.immortals.auth.service.domain.entity.Roles;
import com.immortals.auth.service.domain.entity.User;
import com.immortals.auth.service.repository.PermissionsRepository;
import com.immortals.auth.service.repository.RoleRepository;
import com.immortals.auth.service.repository.UserRepository;
import com.immortals.auth.service.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EntitlementControllerIT extends AbstractIntegrationTest {

    @Autowired RoleRepository roleRepository;
    @Autowired PermissionsRepository permissionsRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Roles savedRole;
    private Permissions savedPermission;
    private User savedUser;

    @BeforeEach
    void seedEntitlements() {
        savedRole = roleRepository.save(TestDataFactory.buildRole("TESTER"));
        savedPermission = permissionsRepository.save(TestDataFactory.buildPermission("READ_DATA"));
        savedUser = userRepository.save(User.builder()
                .firstName(TestDataFactory.TEST_FIRST_NAME)
                .lastName(TestDataFactory.TEST_LAST_NAME)
                .userName(TestDataFactory.TEST_USERNAME)
                .email(TestDataFactory.TEST_EMAIL)
                .password(passwordEncoder.encode(TestDataFactory.TEST_PASSWORD_RAW))
                .emailVerified(true)
                .phoneNumberVerified(false)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .accountLocked(false)
                .credentialsNonExpired(true)
                .activeInd(true)
                .roles(new java.util.HashSet<>())
                .build());
    }

    // ──────────────────────────── POST /api/v1/entitlements/roles ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoleRequest("NEW_ROLE", "New test role"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NEW_ROLE"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRole_asUser_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoleRequest("NEW_ROLE", "desc"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRole_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoleRequest("NEW_ROLE", "desc"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_withDuplicateName_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateRoleRequest("TESTER", "Duplicate"))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRole_withMissingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"No name\"}"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────── GET /api/v1/entitlements/roles ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRoles_asAdmin_returnsAllRoles() throws Exception {
        mockMvc.perform(get("/api/v1/entitlements/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].name", hasItem("TESTER")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRoles_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/entitlements/roles"))
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────── GET /api/v1/entitlements (roles for current user) ────────────────────────────

    @Test
    @WithMockUser(username = TestDataFactory.TEST_USERNAME)
    void getRolesForCurrentUser_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/entitlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ──────────────────────────── DELETE /api/v1/entitlements/roles/{id} ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/entitlements/roles/" + savedRole.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRole_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/entitlements/roles/999999"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────── POST /api/v1/entitlements/permissions ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPermission_asAdmin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePermissionDto("WRITE_DATA", "Write permission"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("WRITE_DATA"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPermission_whenDuplicate_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePermissionDto("READ_DATA", "Duplicate"))))
                .andExpect(status().isConflict());
    }

    // ──────────────────────────── GET /api/v1/entitlements/permissions ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPermissions_returnsAll() throws Exception {
        mockMvc.perform(get("/api/v1/entitlements/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("READ_DATA")));
    }

    // ──────────────────────────── DELETE /api/v1/entitlements/permissions/{id} ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePermission_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/entitlements/permissions/" + savedPermission.getId()))
                .andExpect(status().isNoContent());
    }

    // ──────────────────────────── POST /api/v1/entitlements/assign-roles ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleToUser_asAdmin_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/assign-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignRoleRequest(savedUser.getId(), savedRole.getId()))))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignRoleToUser_whenUserNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/assign-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignRoleRequest(999999L, savedRole.getId()))))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────── POST /api/v1/entitlements/assign-permissions ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignPermissionToRole_asAdmin_returns204() throws Exception {
        mockMvc.perform(post("/api/v1/entitlements/assign-permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AssignPermissionRequest(savedRole.getId(), savedPermission.getId()))))
                .andExpect(status().isNoContent());
    }

    // ──────────────────────────── DELETE /api/v1/entitlements/users/{userId}/roles/{roleId} ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRoleFromUser_whenUserDoesNotHaveRole_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/entitlements/users/" + savedUser.getId()
                        + "/roles/" + savedRole.getId()))
                .andExpect(status().isNotFound());
    }
}
