package com.rxbuddy.auth.controller;

import com.rxbuddy.auth.dto.LoginRequest;
import com.rxbuddy.auth.dto.LoginResponse;
import com.rxbuddy.auth.dto.RefreshTokenRequest;
import com.rxbuddy.auth.service.AuthService;
import com.rxbuddy.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for phone: {}", request.getPhone());
        LoginResponse response = authService.login(request);

        // Check if this is a tenant selection response
        if (response.getAccessToken() == null && response.getAvailableTenants() != null) {
            return ResponseEntity.ok(ApiResponse.success("Multiple tenants available. Please select one.", response));
        }

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        // In a production system, you would:
        // 1. Extract the token
        // 2. Add it to a blacklist in Redis
        // For now, we just return success (client should discard the token)
        log.info("Logout request received");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        // This endpoint can be used by other services to validate tokens
        // The Gateway already does this, but this can be used for internal validation
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
        // If the request reached here through the gateway, the token is valid
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}
