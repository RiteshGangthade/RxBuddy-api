package com.rxbuddy.tenant.service;

import com.rxbuddy.tenant.dto.CreateTenantRequest;
import com.rxbuddy.tenant.dto.TenantDTO;
import com.rxbuddy.tenant.entity.Plan;
import com.rxbuddy.tenant.entity.Subscription;
import com.rxbuddy.tenant.entity.Tenant;
import com.rxbuddy.tenant.exception.DuplicateResourceException;
import com.rxbuddy.tenant.exception.ResourceNotFoundException;
import com.rxbuddy.tenant.repository.PlanRepository;
import com.rxbuddy.tenant.repository.SubscriptionRepository;
import com.rxbuddy.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<TenantDTO> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(this::toTenantDTO);
    }

    @Transactional(readOnly = true)
    public TenantDTO getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));
        return toTenantDTO(tenant);
    }

    @Transactional(readOnly = true)
    public TenantDTO getTenantByCode(String code) {
        Tenant tenant = tenantRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + code));
        return toTenantDTO(tenant);
    }

    @Transactional
    public TenantDTO createTenant(CreateTenantRequest request) {
        // Check for duplicate phone
        if (tenantRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // Generate unique code
        String code = generateTenantCode(request.getName());

        // Get plan (default to PRO for trial)
        String planCode = request.getPlanCode() != null ? request.getPlanCode() : "PRO";
        Plan plan = planRepository.findByCode(planCode)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planCode));

        // Create tenant
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .code(code)
                .businessName(request.getBusinessName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gstNumber(request.getGstNumber())
                .drugLicenseNumber(request.getDrugLicenseNumber())
                .panNumber(request.getPanNumber())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .isActive(true)
                .activatedAt(LocalDateTime.now())
                .build();

        tenant = tenantRepository.save(tenant);

        // Create trial subscription (14 days)
        LocalDate today = LocalDate.now();
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .startDate(today)
                .endDate(today.plusDays(14))
                .status(Subscription.SubscriptionStatus.TRIAL)
                .trialEndsAt(today.plusDays(14))
                .billingCycle(Subscription.BillingCycle.YEARLY)
                .build();

        subscriptionRepository.save(subscription);

        log.info("Created new tenant: {} with code: {}", tenant.getName(), tenant.getCode());

        return toTenantDTO(tenant);
    }

    @Transactional
    public TenantDTO updateTenant(Long id, CreateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));

        // Check phone uniqueness if changed
        if (!tenant.getPhone().equals(request.getPhone()) &&
            tenantRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        tenant.setName(request.getName());
        tenant.setBusinessName(request.getBusinessName());
        tenant.setPhone(request.getPhone());
        tenant.setEmail(request.getEmail());
        tenant.setGstNumber(request.getGstNumber());
        tenant.setDrugLicenseNumber(request.getDrugLicenseNumber());
        tenant.setPanNumber(request.getPanNumber());
        tenant.setAddressLine(request.getAddressLine());
        tenant.setCity(request.getCity());
        tenant.setState(request.getState());
        tenant.setPincode(request.getPincode());

        tenant = tenantRepository.save(tenant);

        log.info("Updated tenant: {}", tenant.getCode());

        return toTenantDTO(tenant);
    }

    @Transactional
    public void deactivateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));

        tenant.setIsActive(false);
        tenant.setDeactivatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        log.info("Deactivated tenant: {}", tenant.getCode());
    }

    @Transactional
    public void activateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));

        tenant.setIsActive(true);
        tenant.setActivatedAt(LocalDateTime.now());
        tenant.setDeactivatedAt(null);
        tenantRepository.save(tenant);

        log.info("Activated tenant: {}", tenant.getCode());
    }

    private String generateTenantCode(String name) {
        // Generate code from name prefix + random suffix
        String prefix = name.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (prefix.length() > 4) {
            prefix = prefix.substring(0, 4);
        }
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String code = prefix + suffix;

        // Ensure uniqueness
        int attempt = 0;
        while (tenantRepository.existsByCode(code) && attempt < 10) {
            suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            code = prefix + suffix;
            attempt++;
        }

        return code;
    }

    private TenantDTO toTenantDTO(Tenant tenant) {
        TenantDTO.TenantDTOBuilder builder = TenantDTO.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .code(tenant.getCode())
                .businessName(tenant.getBusinessName())
                .gstNumber(tenant.getGstNumber())
                .drugLicenseNumber(tenant.getDrugLicenseNumber())
                .panNumber(tenant.getPanNumber())
                .phone(tenant.getPhone())
                .email(tenant.getEmail())
                .addressLine(tenant.getAddressLine())
                .city(tenant.getCity())
                .state(tenant.getState())
                .pincode(tenant.getPincode())
                .country(tenant.getCountry())
                .currency(tenant.getCurrency())
                .timezone(tenant.getTimezone())
                .dateFormat(tenant.getDateFormat())
                .logoPath(tenant.getLogoPath())
                .isActive(tenant.getIsActive())
                .activatedAt(tenant.getActivatedAt())
                .createdAt(tenant.getCreatedAt());

        // Get current subscription
        subscriptionRepository.findByTenantIdAndStatus(tenant.getId(), Subscription.SubscriptionStatus.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenant.getId(), Subscription.SubscriptionStatus.TRIAL))
                .ifPresent(sub -> {
                    builder.planName(sub.getPlan().getName());
                    builder.planCode(sub.getPlan().getCode());
                    builder.subscriptionStatus(sub.getStatus().name());
                });

        return builder.build();
    }
}
