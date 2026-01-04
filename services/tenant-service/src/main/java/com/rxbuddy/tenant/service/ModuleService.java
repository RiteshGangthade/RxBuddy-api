package com.rxbuddy.tenant.service;

import com.rxbuddy.tenant.dto.ModuleDTO;
import com.rxbuddy.tenant.dto.TenantModuleDTO;
import com.rxbuddy.tenant.dto.TenantModulesResponse;
import com.rxbuddy.tenant.entity.*;
import com.rxbuddy.tenant.entity.Module;
import com.rxbuddy.tenant.exception.ResourceNotFoundException;
import com.rxbuddy.tenant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final TenantRepository tenantRepository;
    private final TenantModuleRepository tenantModuleRepository;
    private final PlanModuleRepository planModuleRepository;
    private final SubscriptionRepository subscriptionRepository;

    public List<ModuleDTO> getAllModules() {
        return moduleRepository.findAllByOrderByDisplayOrder()
                .stream()
                .map(this::toModuleDTO)
                .collect(Collectors.toList());
    }

    public ModuleDTO getModuleByCode(String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + code));
        return toModuleDTO(module);
    }

    @Transactional(readOnly = true)
    public TenantModulesResponse getTenantModules(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));

        // Get active subscription
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, Subscription.SubscriptionStatus.ACTIVE);

        // If no active, try trial
        Subscription subscription = activeSubscription
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, Subscription.SubscriptionStatus.TRIAL))
                .orElse(null);

        String planName = subscription != null ? subscription.getPlan().getName() : "No Plan";
        String planCode = subscription != null ? subscription.getPlan().getCode() : null;

        // Get modules in plan
        Set<Long> planModuleIds = new HashSet<>();
        if (subscription != null) {
            planModuleIds = planModuleRepository.findByPlanId(subscription.getPlan().getId())
                    .stream()
                    .map(pm -> pm.getModule().getId())
                    .collect(Collectors.toSet());
        }

        // Get tenant overrides
        Map<Long, TenantModule> tenantOverrides = tenantModuleRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(tm -> tm.getModule().getId(), tm -> tm));

        // Build module list
        List<TenantModuleDTO> modules = new ArrayList<>();
        for (Module module : moduleRepository.findAllByOrderByDisplayOrder()) {
            TenantModuleDTO dto = buildTenantModuleDTO(module, planModuleIds, tenantOverrides);
            modules.add(dto);
        }

        return TenantModulesResponse.builder()
                .tenantId(tenantId)
                .tenantName(tenant.getName())
                .planName(planName)
                .planCode(planCode)
                .modules(modules)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ModuleDTO> getEnabledModulesForTenant(Long tenantId) {
        // Verify tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResourceNotFoundException("Tenant not found: " + tenantId);
        }

        return moduleRepository.findEnabledModulesForTenant(tenantId)
                .stream()
                .map(this::toModuleDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isModuleEnabled(Long tenantId, String moduleCode) {
        Module module = moduleRepository.findByCode(moduleCode)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleCode));

        // Core modules are always enabled
        if (Boolean.TRUE.equals(module.getIsCore())) {
            return true;
        }

        // Check for override
        Optional<TenantModule> override = tenantModuleRepository.findByTenantIdAndModuleId(tenantId, module.getId());
        if (override.isPresent()) {
            return Boolean.TRUE.equals(override.get().getIsEnabled());
        }

        // Check plan modules
        Optional<Subscription> subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, Subscription.SubscriptionStatus.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, Subscription.SubscriptionStatus.TRIAL));

        if (subscription.isPresent()) {
            return planModuleRepository.existsByPlanIdAndModuleId(
                    subscription.get().getPlan().getId(), module.getId());
        }

        return false;
    }

    @Transactional
    public TenantModuleDTO enableModule(Long tenantId, Long moduleId, Long adminUserId, String notes) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        // Core modules cannot be overridden
        if (Boolean.TRUE.equals(module.getIsCore())) {
            throw new IllegalArgumentException("Core modules cannot be modified");
        }

        TenantModule tenantModule = tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .orElse(TenantModule.builder()
                        .tenant(tenant)
                        .module(module)
                        .build());

        tenantModule.setIsEnabled(true);
        tenantModule.setEnabledBy(adminUserId);
        tenantModule.setEnabledAt(LocalDateTime.now());
        tenantModule.setDisabledBy(null);
        tenantModule.setDisabledAt(null);
        tenantModule.setNotes(notes);

        tenantModuleRepository.save(tenantModule);

        log.info("Module {} enabled for tenant {} by admin {}", module.getCode(), tenant.getCode(), adminUserId);

        return TenantModuleDTO.builder()
                .code(module.getCode())
                .name(module.getName())
                .isCore(module.getIsCore())
                .isEnabled(true)
                .source(TenantModuleDTO.ModuleSource.OVERRIDE)
                .enabledAt(tenantModule.getEnabledAt())
                .notes(notes)
                .build();
    }

    @Transactional
    public TenantModuleDTO disableModule(Long tenantId, Long moduleId, Long adminUserId, String notes) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + tenantId));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        // Core modules cannot be disabled
        if (Boolean.TRUE.equals(module.getIsCore())) {
            throw new IllegalArgumentException("Core modules cannot be disabled");
        }

        TenantModule tenantModule = tenantModuleRepository.findByTenantIdAndModuleId(tenantId, moduleId)
                .orElse(TenantModule.builder()
                        .tenant(tenant)
                        .module(module)
                        .build());

        tenantModule.setIsEnabled(false);
        tenantModule.setDisabledBy(adminUserId);
        tenantModule.setDisabledAt(LocalDateTime.now());
        tenantModule.setNotes(notes);

        tenantModuleRepository.save(tenantModule);

        log.info("Module {} disabled for tenant {} by admin {}", module.getCode(), tenant.getCode(), adminUserId);

        return TenantModuleDTO.builder()
                .code(module.getCode())
                .name(module.getName())
                .isCore(module.getIsCore())
                .isEnabled(false)
                .source(TenantModuleDTO.ModuleSource.OVERRIDE)
                .disabledAt(tenantModule.getDisabledAt())
                .notes(notes)
                .build();
    }

    private TenantModuleDTO buildTenantModuleDTO(Module module, Set<Long> planModuleIds,
                                                  Map<Long, TenantModule> tenantOverrides) {
        TenantModuleDTO.TenantModuleDTOBuilder builder = TenantModuleDTO.builder()
                .code(module.getCode())
                .name(module.getName())
                .description(module.getDescription())
                .icon(module.getIcon())
                .isCore(module.getIsCore());

        // Determine source and enabled status
        if (Boolean.TRUE.equals(module.getIsCore())) {
            builder.source(TenantModuleDTO.ModuleSource.CORE)
                   .isEnabled(true);
        } else if (tenantOverrides.containsKey(module.getId())) {
            TenantModule override = tenantOverrides.get(module.getId());
            builder.source(TenantModuleDTO.ModuleSource.OVERRIDE)
                   .isEnabled(override.getIsEnabled())
                   .enabledAt(override.getEnabledAt())
                   .disabledAt(override.getDisabledAt())
                   .notes(override.getNotes());
        } else if (planModuleIds.contains(module.getId())) {
            builder.source(TenantModuleDTO.ModuleSource.PLAN)
                   .isEnabled(true);
        } else {
            builder.source(TenantModuleDTO.ModuleSource.PLAN)
                   .isEnabled(false);
        }

        return builder.build();
    }

    private ModuleDTO toModuleDTO(Module module) {
        return ModuleDTO.builder()
                .id(module.getId())
                .code(module.getCode())
                .name(module.getName())
                .description(module.getDescription())
                .isCore(module.getIsCore())
                .displayOrder(module.getDisplayOrder())
                .icon(module.getIcon())
                .build();
    }
}
