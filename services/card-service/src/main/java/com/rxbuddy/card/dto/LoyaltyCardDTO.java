package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyCardDTO {
    private Long id;
    private String cardNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private BigDecimal pointsBalance;
    private BigDecimal totalPointsEarned;
    private BigDecimal totalPointsRedeemed;
    private BigDecimal totalReferralPointsEarned;
    private Boolean isActive;
    private LocalDateTime issuedAt;
    private LocalDateTime lastTransactionAt;

    // Referrer info
    private Long referrerCardId;
    private String referrerCardNumber;
    private String referrerName;

    // Count of referred customers
    private Integer referralCount;
}
