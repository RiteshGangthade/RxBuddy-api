package com.rxbuddy.card.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private Long tenantId;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = false;

    @Column(name = "points_to_amount_rate", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointsToAmountRate = new BigDecimal("0.10");

    @Column(name = "max_redemption_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxRedemptionPercent = new BigDecimal("50.00");

    @Column(name = "min_points_to_redeem")
    @Builder.Default
    private Integer minPointsToRedeem = 100;

    @Column(name = "referral_points_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal referralPointsPercent = new BigDecimal("0.50");

    @Column(name = "referral_enabled")
    @Builder.Default
    private Boolean referralEnabled = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
