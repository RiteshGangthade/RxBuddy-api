package com.rxbuddy.card.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "loyalty_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "card_number", nullable = false, length = 50)
    private String cardNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Customer Info (denormalized for quick access)
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    // Referral (who referred this customer - only direct referrer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_card_id")
    private LoyaltyCard referrerCard;

    // Customers referred by this card
    @OneToMany(mappedBy = "referrerCard", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<LoyaltyCard> referredCards = new HashSet<>();

    // Points Balance
    @Column(name = "points_balance", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pointsBalance = BigDecimal.ZERO;

    @Column(name = "total_points_earned", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPointsEarned = BigDecimal.ZERO;

    @Column(name = "total_points_redeemed", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPointsRedeemed = BigDecimal.ZERO;

    @Column(name = "total_referral_points_earned", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalReferralPointsEarned = BigDecimal.ZERO;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "issued_at")
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Transactions
    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PointTransaction> transactions = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addPoints(BigDecimal points) {
        this.pointsBalance = this.pointsBalance.add(points);
        this.totalPointsEarned = this.totalPointsEarned.add(points);
        this.lastTransactionAt = LocalDateTime.now();
    }

    public void addReferralPoints(BigDecimal points) {
        this.pointsBalance = this.pointsBalance.add(points);
        this.totalReferralPointsEarned = this.totalReferralPointsEarned.add(points);
        this.lastTransactionAt = LocalDateTime.now();
    }

    public void redeemPoints(BigDecimal points) {
        if (this.pointsBalance.compareTo(points) < 0) {
            throw new IllegalArgumentException("Insufficient points balance");
        }
        this.pointsBalance = this.pointsBalance.subtract(points);
        this.totalPointsRedeemed = this.totalPointsRedeemed.add(points);
        this.lastTransactionAt = LocalDateTime.now();
    }
}
