package com.rxbuddy.tenant.controller;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.common.dto.PageResponse;
import com.rxbuddy.tenant.dto.CreateTenantRequest;
import com.rxbuddy.tenant.dto.TenantDTO;
import com.rxbuddy.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TenantDTO>>> getAllTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Page<TenantDTO> tenants = tenantService.getAllTenants(PageRequest.of(page, size, sort));

        PageResponse<TenantDTO> response = PageResponse.<TenantDTO>builder()
                .content(tenants.getContent())
                .page(tenants.getNumber())
                .size(tenants.getSize())
                .totalElements(tenants.getTotalElements())
                .totalPages(tenants.getTotalPages())
                .first(tenants.isFirst())
                .last(tenants.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantById(@PathVariable Long id) {
        TenantDTO tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(tenant));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<TenantDTO>> getTenantByCode(@PathVariable String code) {
        TenantDTO tenant = tenantService.getTenantByCode(code);
        return ResponseEntity.ok(ApiResponse.success(tenant));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TenantDTO>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        TenantDTO tenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant created successfully", tenant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantDTO>> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody CreateTenantRequest request) {
        TenantDTO tenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant updated successfully", tenant));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateTenant(@PathVariable Long id) {
        tenantService.activateTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant activated successfully", null));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateTenant(@PathVariable Long id) {
        tenantService.deactivateTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deactivated successfully", null));
    }
}
