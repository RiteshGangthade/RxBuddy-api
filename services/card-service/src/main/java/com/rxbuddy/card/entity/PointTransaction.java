package com.rxbuddy.card.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private LoyaltyCard card;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal points;

    @Column(name = "balance_after", precision = 12, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    // Reference
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    // For earned points
    @Column(name = "bill_amount", precision = 12, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "point_percentage", precision = 5, scale = 2)
    private BigDecimal pointPercentage;

    // For referral earned
    @Column(name = "referred_card_id")
    private Long referredCardId;

    @Column(name = "referred_bill_id")
    private Long referredBillId;

    @Column(length = 500)
    private String description;

    @Column(name = "performed_by")
    private Long performedBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        EARNED,
        REDEEMED,
        REFERRAL_EARNED,
        EXPIRED,
        ADJUSTED
    }
}
