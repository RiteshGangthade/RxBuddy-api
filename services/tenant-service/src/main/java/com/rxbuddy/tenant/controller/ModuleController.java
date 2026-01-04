package com.rxbuddy.tenant.controller;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.tenant.dto.ModuleDTO;
import com.rxbuddy.tenant.dto.ModuleToggleRequest;
import com.rxbuddy.tenant.dto.TenantModuleDTO;
import com.rxbuddy.tenant.dto.TenantModulesResponse;
import com.rxbuddy.tenant.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    // ==================== Super Admin APIs ====================

    @GetMapping("/admin/modules")
    public ResponseEntity<ApiResponse<List<ModuleDTO>>> getAllModules() {
        List<ModuleDTO> modules = moduleService.getAllModules();
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/admin/modules/{code}")
    public ResponseEntity<ApiResponse<ModuleDTO>> getModuleByCode(@PathVariable String code) {
        ModuleDTO module = moduleService.getModuleByCode(code);
        return ResponseEntity.ok(ApiResponse.success(module));
    }

    @GetMapping("/admin/tenants/{tenantId}/modules")
    public ResponseEntity<ApiResponse<TenantModulesResponse>> getTenantModules(
            @PathVariable Long tenantId) {
        TenantModulesResponse response = moduleService.getTenantModules(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/admin/tenants/{tenantId}/modules/{moduleId}/enable")
    public ResponseEntity<ApiResponse<TenantModuleDTO>> enableModule(
            @PathVariable Long tenantId,
            @PathVariable Long moduleId,
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId,
            @RequestBody(required = false) ModuleToggleRequest request) {

        String notes = request != null ? request.getNotes() : null;
        TenantModuleDTO result = moduleService.enableModule(tenantId, moduleId, adminUserId, notes);
        return ResponseEntity.ok(ApiResponse.success("Module enabled successfully", result));
    }

    @PostMapping("/admin/tenants/{tenantId}/modules/{moduleId}/disable")
    public ResponseEntity<ApiResponse<TenantModuleDTO>> disableModule(
            @PathVariable Long tenantId,
            @PathVariable Long moduleId,
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId,
            @RequestBody(required = false) ModuleToggleRequest request) {

        String notes = request != null ? request.getNotes() : null;
        TenantModuleDTO result = moduleService.disableModule(tenantId, moduleId, adminUserId, notes);
        return ResponseEntity.ok(ApiResponse.success("Module disabled successfully", result));
    }

    // ==================== Tenant APIs ====================

    @GetMapping("/modules")
    public ResponseEntity<ApiResponse<List<ModuleDTO>>> getEnabledModules(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<ModuleDTO> modules = moduleService.getEnabledModulesForTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/modules/{code}/enabled")
    public ResponseEntity<ApiResponse<Boolean>> isModuleEnabled(
            @PathVariable String code,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        boolean enabled = moduleService.isModuleEnabled(tenantId, code);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }
}
