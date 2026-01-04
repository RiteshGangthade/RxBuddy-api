package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransactionDTO {
    private Long id;
    private String transactionType;
    private BigDecimal points;
    private BigDecimal balanceAfter;
    private String referenceType;
    private Long referenceId;
    private BigDecimal billAmount;
    private String categoryName;
    private BigDecimal pointPercentage;
    private String description;
    private LocalDateTime createdAt;
}
