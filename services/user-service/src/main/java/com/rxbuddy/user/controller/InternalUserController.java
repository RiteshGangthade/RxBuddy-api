package com.rxbuddy.user.controller;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.common.dto.UserDTO;
import com.rxbuddy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API endpoints for service-to-service communication.
 * These endpoints are called by other microservices (e.g., auth-service).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/users/by-phone/{phone}")
    public ResponseEntity<ApiResponse<UserDTO>> findByPhone(@PathVariable String phone) {
        log.debug("Internal request: find user by phone: {}", phone);
        UserDTO user = userService.findByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/users/{id}/tenants")
    public ResponseEntity<ApiResponse<List<UserService.UserTenantInfo>>> getUserTenants(@PathVariable Long id) {
        log.debug("Internal request: get tenants for user: {}", id);
        List<UserService.UserTenantInfo> tenants = userService.getUserTenants(id);
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @GetMapping("/users/{id}/tenant/{tenantId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserWithTenantContext(
            @PathVariable Long id,
            @PathVariable Long tenantId) {
        log.debug("Internal request: get user {} with tenant context {}", id, tenantId);
        UserDTO user = userService.getUserWithTenantContext(id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
