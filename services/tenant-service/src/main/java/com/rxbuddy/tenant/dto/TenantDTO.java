package com.rxbuddy.tenant.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDTO {
    private Long id;
    private String name;
    private String code;
    private String businessName;
    private String gstNumber;
    private String drugLicenseNumber;
    private String panNumber;
    private String phone;
    private String email;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private String currency;
    private String timezone;
    private String dateFormat;
    private String logoPath;
    private Boolean isActive;
    private LocalDateTime activatedAt;
    private LocalDateTime createdAt;

    // Current subscription info (denormalized for convenience)
    private String planName;
    private String planCode;
    private String subscriptionStatus;
}
