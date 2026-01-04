package com.rxbuddy.card.controller;

import com.rxbuddy.card.dto.*;
import com.rxbuddy.card.service.LoyaltyCardService;
import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class LoyaltyCardController {

    private final LoyaltyCardService cardService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoyaltyCardDTO>>> getAllCards(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Page<LoyaltyCardDTO> cards;
        if (search != null && !search.isBlank()) {
            cards = cardService.searchCards(tenantId, search, PageRequest.of(page, size));
        } else {
            cards = cardService.getAllCards(tenantId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        PageResponse<LoyaltyCardDTO> response = PageResponse.<LoyaltyCardDTO>builder()
                .content(cards.getContent())
                .page(cards.getNumber())
                .size(cards.getSize())
                .totalElements(cards.getTotalElements())
                .totalPages(cards.getTotalPages())
                .first(cards.isFirst())
                .last(cards.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> getCardById(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
        LoyaltyCardDTO card = cardService.getCardById(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success(card));
    }

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

    @PostMapping
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> createCard(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @Valid @RequestBody CreateCardRequest request) {
        LoyaltyCardDTO card = cardService.createCard(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Card created successfully", card));
    }

    @PostMapping("/{id}/link-referrer")
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> linkReferrer(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id,
            @RequestParam String referrerCardNumber) {
        LoyaltyCardDTO card = cardService.linkReferrer(tenantId, id, referrerCardNumber);
        return ResponseEntity.ok(ApiResponse.success("Referrer linked successfully", card));
    }

    @GetMapping("/{id}/referrals")
    public ResponseEntity<ApiResponse<List<LoyaltyCardDTO>>> getReferrals(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
        List<LoyaltyCardDTO> referrals = cardService.getReferrals(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success(referrals));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<PageResponse<PointTransactionDTO>>> getTransactions(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PointTransactionDTO> transactions = cardService.getTransactions(
                tenantId, id, PageRequest.of(page, size));

        PageResponse<PointTransactionDTO> response = PageResponse.<PointTransactionDTO>builder()
                .content(transactions.getContent())
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .first(transactions.isFirst())
                .last(transactions.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<LoyaltyCardDTO>> deactivateCard(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @PathVariable Long id) {
        LoyaltyCardDTO card = cardService.deactivateCard(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success("Card deactivated successfully", card));
    }
}
