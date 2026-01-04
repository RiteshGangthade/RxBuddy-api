package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardConfigurationDTO {
    private Long tenantId;
    private Boolean isEnabled;
    private BigDecimal pointsToAmountRate;
    private BigDecimal maxRedemptionPercent;
    private Integer minPointsToRedeem;
    private BigDecimal referralPointsPercent;
    private Boolean referralEnabled;
    private List<CategoryPointConfigDTO> categoryPointConfigs;
    private List<CategoryDiscountDTO> categoryDiscounts;
}
