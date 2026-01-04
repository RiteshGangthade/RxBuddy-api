package com.rxbuddy.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String passwordHash;
    private boolean isActive;
    private Long tenantId;
    private String role;
    private Set<String> permissions;
}
