package com.rxbuddy.user.service;

import com.rxbuddy.common.dto.UserDTO;
import com.rxbuddy.user.entity.RolePermission;
import com.rxbuddy.user.entity.User;
import com.rxbuddy.user.entity.UserTenant;
import com.rxbuddy.user.exception.ResourceNotFoundException;
import com.rxbuddy.user.repository.UserRepository;
import com.rxbuddy.user.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;

    @Transactional(readOnly = true)
    public UserDTO findByPhone(String phone) {
        log.debug("Finding user by phone: {}", phone);

        User user = userRepository.findByPhoneAndDeletedAtIsNull(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + phone));

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserTenantInfo> getUserTenants(Long userId) {
        log.debug("Getting tenants for user: {}", userId);

        List<UserTenant> userTenants = userTenantRepository.findByUserIdAndIsActiveTrue(userId);

        return userTenants.stream()
                .map(ut -> new UserTenantInfo(
                        ut.getTenantId(),
                        ut.getTenantName(),
                        ut.getRole().getCode()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserWithTenantContext(Long userId, Long tenantId) {
        log.debug("Getting user {} with tenant context {}", userId, tenantId);

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserTenant userTenant = userTenantRepository.findByUserIdAndTenantIdWithPermissions(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not associated with tenant: " + tenantId));

        Set<String> permissions = userTenant.getRole().getRolePermissions().stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .tenantId(tenantId)
                .role(userTenant.getRole().getCode())
                .permissions(permissions)
                .build();
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .isActive(user.getIsActive())
                .build();
    }

    public record UserTenantInfo(Long tenantId, String tenantName, String role) {}
}
