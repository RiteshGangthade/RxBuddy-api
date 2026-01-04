package com.rxbuddy.tenant.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxUsers;
    private Integer maxProducts;
    private Integer maxCustomers;
    private Boolean isActive;
    private Integer displayOrder;
    private List<String> moduleCodes;
}
