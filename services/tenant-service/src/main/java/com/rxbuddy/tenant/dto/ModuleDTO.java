package com.rxbuddy.tenant.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isCore;
    private Integer displayOrder;
    private String icon;
}
