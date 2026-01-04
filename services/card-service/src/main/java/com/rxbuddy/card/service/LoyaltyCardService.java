package com.rxbuddy.card.service;

import com.rxbuddy.card.dto.*;
import com.rxbuddy.card.entity.CardConfiguration;
import com.rxbuddy.card.entity.LoyaltyCard;
import com.rxbuddy.card.entity.PointTransaction;
import com.rxbuddy.card.exception.CardServiceException;
import com.rxbuddy.card.exception.ResourceNotFoundException;
import com.rxbuddy.card.repository.CardConfigurationRepository;
import com.rxbuddy.card.repository.LoyaltyCardRepository;
import com.rxbuddy.card.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyCardService {

    private final LoyaltyCardRepository cardRepository;
    private final CardConfigurationRepository configRepository;
    private final PointTransactionRepository transactionRepository;
    private final CardConfigurationService configService;

    @Transactional(readOnly = true)
    public Page<LoyaltyCardDTO> getAllCards(Long tenantId, Pageable pageable) {
        return cardRepository.findByTenantId(tenantId, pageable)
                .map(this::toCardDTO);
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyCardDTO> searchCards(Long tenantId, String search, Pageable pageable) {
        return cardRepository.searchCards(tenantId, search, pageable)
                .map(this::toCardDTO);
    }

    @Transactional(readOnly = true)
    public LoyaltyCardDTO getCardById(Long tenantId, Long cardId) {
        LoyaltyCard card = cardRepository.findById(cardId)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        return toCardDTO(card);
    }

    @Transactional(readOnly = true)
    public LoyaltyCardDTO getCardByNumber(Long tenantId, String cardNumber) {
        LoyaltyCard card = cardRepository.findByTenantIdAndCardNumber(tenantId, cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardNumber));
        return toCardDTO(card);
    }

    @Transactional(readOnly = true)
    public LoyaltyCardDTO getCardByCustomerId(Long tenantId, Long customerId) {
        LoyaltyCard card = cardRepository.findByTenantIdAndCustomerId(tenantId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found for customer: " + customerId));
        return toCardDTO(card);
    }

    @Transactional
    public LoyaltyCardDTO createCard(Long tenantId, CreateCardRequest request) {
        // Check if card system is enabled
        if (!configService.isCardSystemEnabled(tenantId)) {
            throw new CardServiceException("Card system is not enabled for this tenant");
        }

        // Check if customer already has a card
        if (cardRepository.existsByTenantIdAndCustomerId(tenantId, request.getCustomerId())) {
            throw new CardServiceException("Customer already has a loyalty card");
        }

        // Generate unique card number
        String cardNumber = generateCardNumber(tenantId);

        // Find referrer if provided
        LoyaltyCard referrerCard = null;
        if (request.getReferrerCardNumber() != null && !request.getReferrerCardNumber().isBlank()) {
            referrerCard = cardRepository.findByTenantIdAndCardNumber(tenantId, request.getReferrerCardNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Referrer card not found: " + request.getReferrerCardNumber()));
        }

        LoyaltyCard card = LoyaltyCard.builder()
                .tenantId(tenantId)
                .cardNumber(cardNumber)
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .referrerCard(referrerCard)
                .isActive(true)
                .build();

        card = cardRepository.save(card);
        log.info("Created loyalty card {} for customer {} in tenant {}", cardNumber, request.getCustomerId(), tenantId);

        return toCardDTO(card);
    }

    @Transactional
    public LoyaltyCardDTO linkReferrer(Long tenantId, Long cardId, String referrerCardNumber) {
        LoyaltyCard card = cardRepository.findById(cardId)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (card.getReferrerCard() != null) {
            throw new CardServiceException("Card already has a referrer linked");
        }

        LoyaltyCard referrerCard = cardRepository.findByTenantIdAndCardNumber(tenantId, referrerCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Referrer card not found: " + referrerCardNumber));

        if (referrerCard.getId().equals(cardId)) {
            throw new CardServiceException("Cannot link card to itself as referrer");
        }

        card.setReferrerCard(referrerCard);
        card = cardRepository.save(card);

        log.info("Linked referrer {} to card {} in tenant {}", referrerCardNumber, card.getCardNumber(), tenantId);
        return toCardDTO(card);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyCardDTO> getReferrals(Long tenantId, Long cardId) {
        return cardRepository.findReferrals(tenantId, cardId)
                .stream()
                .map(this::toCardDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EarnPointsResponse earnPoints(Long tenantId, EarnPointsRequest request) {
        // Get card
        LoyaltyCard card = cardRepository.findByTenantIdAndCardNumber(tenantId, request.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardNumber()));

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new CardServiceException("Card is not active");
        }

        // Get configuration
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new CardServiceException("Card system not configured"));

        if (!Boolean.TRUE.equals(config.getIsEnabled())) {
            throw new CardServiceException("Card system is not enabled");
        }

        // Calculate points for each category
        BigDecimal totalPoints = BigDecimal.ZERO;
        for (EarnPointsRequest.BillItem item : request.getItems()) {
            BigDecimal percentage = configService.getPointPercentage(tenantId, item.getCategoryId());
            BigDecimal points = item.getAmount()
                    .multiply(percentage)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalPoints = totalPoints.add(points);
        }

        // Add points to card
        card.addPoints(totalPoints);

        // Create transaction record
        PointTransaction transaction = PointTransaction.builder()
                .tenantId(tenantId)
                .card(card)
                .transactionType(PointTransaction.TransactionType.EARNED)
                .points(totalPoints)
                .balanceAfter(card.getPointsBalance())
                .referenceType("BILL")
                .referenceId(request.getBillId())
                .billAmount(request.getBillAmount())
                .description("Points earned from bill #" + request.getBillId())
                .performedBy(request.getPerformedBy())
                .build();

        transactionRepository.save(transaction);
        cardRepository.save(card);

        // Handle referral points (only if referrer exists and referral is enabled)
        BigDecimal referrerPoints = BigDecimal.ZERO;
        String referrerCardNumber = null;

        if (card.getReferrerCard() != null && Boolean.TRUE.equals(config.getReferralEnabled())) {
            LoyaltyCard referrerCard = card.getReferrerCard();

            // Calculate referral points
            referrerPoints = request.getBillAmount()
                    .multiply(config.getReferralPointsPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            if (referrerPoints.compareTo(BigDecimal.ZERO) > 0) {
                referrerCard.addReferralPoints(referrerPoints);

                // Create referral transaction
                PointTransaction referralTx = PointTransaction.builder()
                        .tenantId(tenantId)
                        .card(referrerCard)
                        .transactionType(PointTransaction.TransactionType.REFERRAL_EARNED)
                        .points(referrerPoints)
                        .balanceAfter(referrerCard.getPointsBalance())
                        .referenceType("REFERRAL")
                        .referenceId(request.getBillId())
                        .referredCardId(card.getId())
                        .referredBillId(request.getBillId())
                        .billAmount(request.getBillAmount())
                        .description("Referral points from " + card.getCustomerName() + "'s purchase")
                        .performedBy(request.getPerformedBy())
                        .build();

                transactionRepository.save(referralTx);
                cardRepository.save(referrerCard);
                referrerCardNumber = referrerCard.getCardNumber();
            }
        }

        log.info("Earned {} points for card {} in tenant {} (bill {})",
                totalPoints, request.getCardNumber(), tenantId, request.getBillId());

        return EarnPointsResponse.builder()
                .pointsEarned(totalPoints)
                .newBalance(card.getPointsBalance())
                .referrerPointsEarned(referrerPoints)
                .referrerCardNumber(referrerCardNumber)
                .build();
    }

    @Transactional
    public RedeemPointsResponse redeemPoints(Long tenantId, RedeemPointsRequest request) {
        // Get card
        LoyaltyCard card = cardRepository.findByTenantIdAndCardNumber(tenantId, request.getCardNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardNumber()));

        if (!Boolean.TRUE.equals(card.getIsActive())) {
            throw new CardServiceException("Card is not active");
        }

        // Get configuration
        CardConfiguration config = configRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new CardServiceException("Card system not configured"));

        if (!Boolean.TRUE.equals(config.getIsEnabled())) {
            throw new CardServiceException("Card system is not enabled");
        }

        // Validate minimum points
        if (request.getPoints().compareTo(new BigDecimal(config.getMinPointsToRedeem())) < 0) {
            throw new CardServiceException("Minimum " + config.getMinPointsToRedeem() + " points required to redeem");
        }

        // Validate balance
        if (card.getPointsBalance().compareTo(request.getPoints()) < 0) {
            throw new CardServiceException("Insufficient points balance");
        }

        // Calculate amount
        BigDecimal amountDeducted = request.getPoints().multiply(config.getPointsToAmountRate());

        // Validate max redemption
        BigDecimal maxRedemptionAmount = request.getBillAmount()
                .multiply(config.getMaxRedemptionPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (amountDeducted.compareTo(maxRedemptionAmount) > 0) {
            throw new CardServiceException("Redemption amount exceeds maximum allowed (" +
                    config.getMaxRedemptionPercent() + "% of bill)");
        }

        // Redeem points
        card.redeemPoints(request.getPoints());

        // Create transaction
        PointTransaction transaction = PointTransaction.builder()
                .tenantId(tenantId)
                .card(card)
                .transactionType(PointTransaction.TransactionType.REDEEMED)
                .points(request.getPoints().negate())
                .balanceAfter(card.getPointsBalance())
                .referenceType("BILL")
                .referenceId(request.getBillId())
                .billAmount(request.getBillAmount())
                .description("Points redeemed for bill #" + request.getBillId())
                .performedBy(request.getPerformedBy())
                .build();

        transactionRepository.save(transaction);
        cardRepository.save(card);

        log.info("Redeemed {} points (₹{}) for card {} in tenant {} (bill {})",
                request.getPoints(), amountDeducted, request.getCardNumber(), tenantId, request.getBillId());

        return RedeemPointsResponse.builder()
                .pointsRedeemed(request.getPoints())
                .amountDeducted(amountDeducted)
                .newBalance(card.getPointsBalance())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PointTransactionDTO> getTransactions(Long tenantId, Long cardId, Pageable pageable) {
        // Verify card belongs to tenant
        cardRepository.findById(cardId)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        return transactionRepository.findByCardIdOrderByCreatedAtDesc(cardId, pageable)
                .map(this::toTransactionDTO);
    }

    @Transactional
    public LoyaltyCardDTO deactivateCard(Long tenantId, Long cardId) {
        LoyaltyCard card = cardRepository.findById(cardId)
                .filter(c -> c.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        card.setIsActive(false);
        card = cardRepository.save(card);

        log.info("Deactivated card {} in tenant {}", card.getCardNumber(), tenantId);
        return toCardDTO(card);
    }

    private String generateCardNumber(Long tenantId) {
        // Format: RXB-TENANT-RANDOM (e.g., RXB-001-ABC123)
        String tenantPart = String.format("%03d", tenantId % 1000);
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String cardNumber = "RXB-" + tenantPart + "-" + randomPart;

        // Ensure uniqueness
        int attempt = 0;
        while (cardRepository.existsByTenantIdAndCardNumber(tenantId, cardNumber) && attempt < 10) {
            randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            cardNumber = "RXB-" + tenantPart + "-" + randomPart;
            attempt++;
        }

        return cardNumber;
    }

    private LoyaltyCardDTO toCardDTO(LoyaltyCard card) {
        LoyaltyCardDTO.LoyaltyCardDTOBuilder builder = LoyaltyCardDTO.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .customerId(card.getCustomerId())
                .customerName(card.getCustomerName())
                .customerPhone(card.getCustomerPhone())
                .customerEmail(card.getCustomerEmail())
                .pointsBalance(card.getPointsBalance())
                .totalPointsEarned(card.getTotalPointsEarned())
                .totalPointsRedeemed(card.getTotalPointsRedeemed())
                .totalReferralPointsEarned(card.getTotalReferralPointsEarned())
                .isActive(card.getIsActive())
                .issuedAt(card.getIssuedAt())
                .lastTransactionAt(card.getLastTransactionAt());

        if (card.getReferrerCard() != null) {
            builder.referrerCardId(card.getReferrerCard().getId())
                   .referrerCardNumber(card.getReferrerCard().getCardNumber())
                   .referrerName(card.getReferrerCard().getCustomerName());
        }

        // Get referral count
        List<LoyaltyCard> referrals = cardRepository.findReferrals(card.getTenantId(), card.getId());
        builder.referralCount(referrals.size());

        return builder.build();
    }

    private PointTransactionDTO toTransactionDTO(PointTransaction tx) {
        return PointTransactionDTO.builder()
                .id(tx.getId())
                .transactionType(tx.getTransactionType().name())
                .points(tx.getPoints())
                .balanceAfter(tx.getBalanceAfter())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .billAmount(tx.getBillAmount())
                .categoryName(tx.getCategoryName())
                .pointPercentage(tx.getPointPercentage())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
