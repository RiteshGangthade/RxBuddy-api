package com.rxbuddy.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EarnPointsRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Bill ID is required")
    private Long billId;

    @NotNull(message = "Bill amount is required")
    private BigDecimal billAmount;

    @NotNull(message = "Items are required")
    private List<BillItem> items;

    private Long performedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillItem {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
    }
}
