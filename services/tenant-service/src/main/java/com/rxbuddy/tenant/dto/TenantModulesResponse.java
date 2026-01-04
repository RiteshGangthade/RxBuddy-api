package com.rxbuddy.tenant.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantModulesResponse {
    private Long tenantId;
    private String tenantName;
    private String planName;
    private String planCode;
    private List<TenantModuleDTO> modules;
}
