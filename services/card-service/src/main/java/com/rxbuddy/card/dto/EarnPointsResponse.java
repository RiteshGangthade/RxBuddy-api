package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarnPointsResponse {
    private BigDecimal pointsEarned;
    private BigDecimal newBalance;
    private BigDecimal referrerPointsEarned;
    private String referrerCardNumber;
}
