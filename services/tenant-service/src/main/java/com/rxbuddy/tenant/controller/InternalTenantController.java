package com.rxbuddy.tenant.controller;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.tenant.dto.ModuleDTO;
import com.rxbuddy.tenant.dto.TenantDTO;
import com.rxbuddy.tenant.service.ModuleService;
import com.rxbuddy.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalTenantController {

    private final TenantService tenantService;
    private final ModuleService moduleService;

    @GetMapping("/tenants/{id}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantById(@PathVariable Long id) {
        TenantDTO tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(tenant));
    }

    @GetMapping("/tenants/code/{code}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantByCode(@PathVariable String code) {
        TenantDTO tenant = tenantService.getTenantByCode(code);
        return ResponseEntity.ok(ApiResponse.success(tenant));
    }

    @GetMapping("/tenants/{tenantId}/modules")
    public ResponseEntity<ApiResponse<List<ModuleDTO>>> getEnabledModules(@PathVariable Long tenantId) {
        List<ModuleDTO> modules = moduleService.getEnabledModulesForTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/tenants/{tenantId}/modules/{code}/enabled")
    public ResponseEntity<Boolean> isModuleEnabled(
            @PathVariable Long tenantId,
            @PathVariable String code) {
        boolean enabled = moduleService.isModuleEnabled(tenantId, code);
        return ResponseEntity.ok(enabled);
    }
}
