package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDiscountDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal discountPercentage;
    private Boolean isActive;
}
