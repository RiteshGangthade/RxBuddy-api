package com.rxbuddy.auth.client;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/api/v1/internal")
public interface UserServiceClient {

    @GetMapping("/users/by-phone/{phone}")
    ApiResponse<UserDTO> findByPhone(@PathVariable("phone") String phone);

    @GetMapping("/users/{id}/tenants")
    ApiResponse<List<UserTenantDTO>> getUserTenants(@PathVariable("id") Long userId);

    @GetMapping("/users/{id}/tenant/{tenantId}")
    ApiResponse<UserDTO> getUserWithTenantContext(
            @PathVariable("id") Long userId,
            @PathVariable("tenantId") Long tenantId
    );

    record UserTenantDTO(Long tenantId, String tenantName, String role) {}
}
