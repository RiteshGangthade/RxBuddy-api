package com.rxbuddy.card.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPointConfigDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal pointPercentage;
    private Boolean isActive;
}
