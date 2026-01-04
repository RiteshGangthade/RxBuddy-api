package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemPointsResponse {
    private BigDecimal pointsRedeemed;
    private BigDecimal amountDeducted;
    private BigDecimal newBalance;
}
