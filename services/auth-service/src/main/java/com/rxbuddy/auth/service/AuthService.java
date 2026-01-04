package com.rxbuddy.auth.service;

import com.rxbuddy.auth.client.UserServiceClient;
import com.rxbuddy.auth.dto.LoginRequest;
import com.rxbuddy.auth.dto.LoginResponse;
import com.rxbuddy.auth.dto.RefreshTokenRequest;
import com.rxbuddy.auth.exception.AuthenticationException;
import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.common.dto.UserDTO;
import com.rxbuddy.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for phone: {}", request.getPhone());

        // Get user from user-service
        ApiResponse<UserDTO> userResponse = userServiceClient.findByPhone(request.getPhone());

        if (!userResponse.isSuccess() || userResponse.getData() == null) {
            throw new AuthenticationException("Invalid phone number or password");
        }

        UserDTO user = userResponse.getData();

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid phone number or password");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        // Get user's tenants
        ApiResponse<List<UserServiceClient.UserTenantDTO>> tenantsResponse =
                userServiceClient.getUserTenants(user.getId());

        List<UserServiceClient.UserTenantDTO> tenants = tenantsResponse.getData();

        if (tenants == null || tenants.isEmpty()) {
            throw new AuthenticationException("User is not associated with any tenant");
        }

        // Determine which tenant to use
        Long selectedTenantId;
        String selectedRole;
        String selectedTenantName;

        if (request.getTenantId() != null) {
            // User specified a tenant
            UserServiceClient.UserTenantDTO selectedTenant = tenants.stream()
                    .filter(t -> t.tenantId().equals(request.getTenantId()))
                    .findFirst()
                    .orElseThrow(() -> new AuthenticationException("User is not associated with the specified tenant"));

            selectedTenantId = selectedTenant.tenantId();
            selectedRole = selectedTenant.role();
            selectedTenantName = selectedTenant.tenantName();
        } else if (tenants.size() == 1) {
            // Only one tenant, use it
            UserServiceClient.UserTenantDTO tenant = tenants.get(0);
            selectedTenantId = tenant.tenantId();
            selectedRole = tenant.role();
            selectedTenantName = tenant.tenantName();
        } else {
            // Multiple tenants - return list for selection (no token yet)
            return LoginResponse.builder()
                    .availableTenants(tenants.stream()
                            .map(t -> LoginResponse.TenantInfo.builder()
                                    .id(t.tenantId())
                                    .name(t.tenantName())
                                    .role(t.role())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

        // Get full user context with permissions
        ApiResponse<UserDTO> fullUserResponse =
                userServiceClient.getUserWithTenantContext(user.getId(), selectedTenantId);

        UserDTO fullUser = fullUserResponse.getData();

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getPhone(),
                selectedTenantId,
                selectedRole,
                fullUser.getPermissions()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .tenantId(selectedTenantId)
                        .tenantName(selectedTenantName)
                        .role(selectedRole)
                        .permissions(fullUser.getPermissions())
                        .build())
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        // Check token type
        String tokenType = jwtUtil.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new AuthenticationException("Invalid token type");
        }

        // Get user ID from token
        Long userId = jwtUtil.extractUserId(refreshToken);

        // Get user details (we need to re-fetch as permissions might have changed)
        // This is simplified - in production, you'd also verify the refresh token in Redis
        ApiResponse<List<UserServiceClient.UserTenantDTO>> tenantsResponse =
                userServiceClient.getUserTenants(userId);

        if (!tenantsResponse.isSuccess() || tenantsResponse.getData() == null || tenantsResponse.getData().isEmpty()) {
            throw new AuthenticationException("User not found or has no tenants");
        }

        // Use the first tenant for now (simplified)
        UserServiceClient.UserTenantDTO tenant = tenantsResponse.getData().get(0);

        // Get full user context
        ApiResponse<UserDTO> userResponse =
                userServiceClient.getUserWithTenantContext(userId, tenant.tenantId());

        UserDTO user = userResponse.getData();

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(
                userId,
                user.getPhone(),
                tenant.tenantId(),
                tenant.role(),
                user.getPermissions()
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime() / 1000)
                .user(LoginResponse.UserInfo.builder()
                        .id(userId)
                        .name(user.getName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .tenantId(tenant.tenantId())
                        .tenantName(tenant.tenantName())
                        .role(tenant.role())
                        .permissions(user.getPermissions())
                        .build())
                .build();
    }
}
