package com.immortals.auth.service.controller;


import com.immortals.auth.service.domain.dto.LoginRequestDto;
import com.immortals.auth.service.domain.dto.LoginResponseDto;
import com.immortals.auth.service.service.EntitlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/entitlements")
public class EntitlementController {
    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponseDto createRoles(@RequestBody @Valid LoginRequestDto loginDto) {
        return entitlementService.create(loginDto);
    }

    @GetMapping()
    public void getRolesForUser() {
        entitlementService.getRolesForUsers();
    }


    @PostMapping(value = "/assign-roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void assignRolesToUser() {
        entitlementService.assignRolesToUser();
    }
}
