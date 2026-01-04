package com.rxbuddy.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String businessName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String email;
    private String gstNumber;
    private String drugLicenseNumber;
    private String panNumber;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;

    // Initial plan (default to PRO for trial)
    private String planCode;
}
