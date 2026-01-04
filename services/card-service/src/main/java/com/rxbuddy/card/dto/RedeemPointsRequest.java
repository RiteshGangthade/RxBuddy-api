package com.rxbuddy.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemPointsRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Points to redeem is required")
    private BigDecimal points;

    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Bill amount is required")
    private BigDecimal billAmount;

    private Long performedBy;
}
