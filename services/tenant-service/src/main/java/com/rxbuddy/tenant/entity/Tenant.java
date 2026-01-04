package com.rxbuddy.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(name = "drug_license_number", length = 50)
    private String drugLicenseNumber;

    @Column(name = "pan_number", length = 15)
    private String panNumber;

    @Column(nullable = false, length = 15)
    private String phone;

    private String email;

    @Column(name = "address_line", length = 500)
    private String addressLine;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 100)
    @Builder.Default
    private String country = "India";

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "date_format", length = 20)
    @Builder.Default
    private String dateFormat = "dd/MM/yyyy";

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Subscription> subscriptions = new HashSet<>();

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TenantModule> tenantModules = new HashSet<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
