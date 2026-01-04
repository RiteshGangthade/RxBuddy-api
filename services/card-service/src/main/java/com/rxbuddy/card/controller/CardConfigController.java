package com.rxbuddy.card.controller;

import com.rxbuddy.card.dto.CardConfigurationDTO;
import com.rxbuddy.card.dto.CategoryDiscountDTO;
import com.rxbuddy.card.dto.CategoryPointConfigDTO;
import com.rxbuddy.card.service.CardConfigurationService;
import com.rxbuddy.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/card-config")
@RequiredArgsConstructor
public class CardConfigController {

    private final CardConfigurationService configService;

    @GetMapping
    public ResponseEntity<ApiResponse<CardConfigurationDTO>> getConfiguration(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        CardConfigurationDTO config = configService.getConfiguration(tenantId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CardConfigurationDTO>> updateConfiguration(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody CardConfigurationDTO request) {
        CardConfigurationDTO config = configService.updateConfiguration(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Configuration updated successfully", config));
    }

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<CardConfigurationDTO>> enableCardSystem(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        CardConfigurationDTO config = configService.enableCardSystem(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Card system enabled successfully", config));
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<CardConfigurationDTO>> disableCardSystem(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        CardConfigurationDTO config = configService.disableCardSystem(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Card system disabled successfully", config));
    }

    // Category Point Configs
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryPointConfigDTO>>> getCategoryPointConfigs(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<CategoryPointConfigDTO> configs = configService.getCategoryPointConfigs(tenantId);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryPointConfigDTO>> saveCategoryPointConfig(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody CategoryPointConfigDTO request) {
        CategoryPointConfigDTO config = configService.saveCategoryPointConfig(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Category point config saved successfully", config));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryPointConfig(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
        configService.deleteCategoryPointConfig(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success("Category point config deleted successfully", null));
    }

    // Category Discounts
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<CategoryDiscountDTO>>> getCategoryDiscounts(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        List<CategoryDiscountDTO> discounts = configService.getCategoryDiscounts(tenantId);
        return ResponseEntity.ok(ApiResponse.success(discounts));
    }

    @PostMapping("/discounts")
    public ResponseEntity<ApiResponse<CategoryDiscountDTO>> saveCategoryDiscount(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody CategoryDiscountDTO request) {
        CategoryDiscountDTO discount = configService.saveCategoryDiscount(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Category discount saved successfully", discount));
    }

    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategoryDiscount(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
        configService.deleteCategoryDiscount(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success("Category discount deleted successfully", null));
    }
}
