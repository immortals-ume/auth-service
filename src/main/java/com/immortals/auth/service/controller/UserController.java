package com.immortals.auth.service.controller;

import com.immortals.auth.service.domain.ApiResponse;
import com.immortals.auth.service.domain.dto.RegisterRequestDTO;
import com.immortals.auth.service.domain.dto.RegisterResponseDTO;
import com.immortals.auth.service.domain.dto.VerifyPhoneOtpRequest;
import com.immortals.auth.service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponseDTO> register(
            @RequestBody @Valid RegisterRequestDTO registerRequest) {

        RegisterResponseDTO response = userService.register(registerRequest);
        return ApiResponse.success(response);
    }

    @PostMapping("/verify-email/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailVerification() {
        userService.sendEmailVerification();
    }

    @GetMapping("/verify-email/confirm")
    public ApiResponse<Void> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ApiResponse.success(null, "Email verified successfully");
    }

    @PostMapping("/verify-phone/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendPhoneOtp() {
        userService.sendPhoneOtp();
    }

    @PostMapping("/verify-phone/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyPhoneOtp(@RequestBody @Valid VerifyPhoneOtpRequest request) {
        userService.verifyPhoneOtp(request.otp());
    }
}
