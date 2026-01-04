package com.rxbuddy.tenant.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantModuleDTO {
    private String code;
    private String name;
    private String description;
    private String icon;
    private Boolean isCore;
    private Boolean isEnabled;
    private ModuleSource source;
    private LocalDateTime enabledAt;
    private String enabledBy;
    private LocalDateTime disabledAt;
    private String disabledBy;
    private String notes;

    public enum ModuleSource {
        CORE,       // Module is core and always enabled
        PLAN,       // Module is enabled via subscription plan
        OVERRIDE    // Module is enabled/disabled via super admin override
    }
}
