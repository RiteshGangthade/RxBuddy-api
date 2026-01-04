package com.rxbuddy.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "enabled_by")
    private Long enabledBy;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "disabled_by")
    private Long disabledBy;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
