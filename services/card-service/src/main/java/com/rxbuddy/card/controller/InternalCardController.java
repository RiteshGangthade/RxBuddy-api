package com.rxbuddy.card.controller;

import com.rxbuddy.card.dto.*;
import com.rxbuddy.card.service.CardConfigurationService;
import com.rxbuddy.card.service.LoyaltyCardService;
import com.rxbuddy.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/cards")
@RequiredArgsConstructor
public class InternalCardController {

    private final LoyaltyCardService cardService;
    private final CardConfigurationService configService;

    @GetMapping("/by-number/{cardNumber}")
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> getCardByNumber(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable String cardNumber) {
        LoyaltyCardDTO card = cardService.getCardByNumber(tenantId, cardNumber);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> getCardByCustomer(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long customerId) {
        LoyaltyCardDTO card = cardService.getCardByCustomerId(tenantId, customerId);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

    @GetMapping("/enabled")
    public ResponseEntity<Boolean> isCardSystemEnabled(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        boolean enabled = configService.isCardSystemEnabled(tenantId);
        return ResponseEntity.ok(enabled);
    }

    @PostMapping("/earn-points")
    public ResponseEntity<ApiResponse<EarnPointsResponse>> earnPoints(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @Valid @RequestBody EarnPointsRequest request) {
        EarnPointsResponse response = cardService.earnPoints(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Points earned successfully", response));
    }

    @PostMapping("/redeem-points")
    public ResponseEntity<ApiResponse<RedeemPointsResponse>> redeemPoints(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @Valid @RequestBody RedeemPointsRequest request) {
        RedeemPointsResponse response = cardService.redeemPoints(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success("Points redeemed successfully", response));
    }
}
