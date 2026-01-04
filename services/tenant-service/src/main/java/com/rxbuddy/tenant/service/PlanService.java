package com.rxbuddy.tenant.service;

import com.rxbuddy.tenant.dto.PlanDTO;
import com.rxbuddy.tenant.entity.Module;
import com.rxbuddy.tenant.entity.Plan;
import com.rxbuddy.tenant.entity.PlanModule;
import com.rxbuddy.tenant.exception.ResourceNotFoundException;
import com.rxbuddy.tenant.repository.ModuleRepository;
import com.rxbuddy.tenant.repository.PlanModuleRepository;
import com.rxbuddy.tenant.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final ModuleRepository moduleRepository;
    private final PlanModuleRepository planModuleRepository;

    public List<PlanDTO> getAllPlans() {
        return planRepository.findByIsActiveTrueOrderByDisplayOrder()
                .stream()
                .map(this::toPlanDTO)
                .collect(Collectors.toList());
    }

    public PlanDTO getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
        return toPlanDTO(plan);
    }

    public PlanDTO getPlanByCode(String code) {
        Plan plan = planRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + code));
        return toPlanDTO(plan);
    }

    public List<String> getPlanModuleCodes(Long planId) {
        return planModuleRepository.findByPlanId(planId)
                .stream()
                .map(pm -> pm.getModule().getCode())
                .collect(Collectors.toList());
    }

    @Transactional
    public void addModuleToPlan(Long planId, Long moduleId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        if (planModuleRepository.existsByPlanIdAndModuleId(planId, moduleId)) {
            log.warn("Module {} already in plan {}", module.getCode(), plan.getCode());
            return;
        }

        PlanModule planModule = PlanModule.builder()
                .plan(plan)
                .module(module)
                .build();

        planModuleRepository.save(planModule);
        log.info("Added module {} to plan {}", module.getCode(), plan.getCode());
    }

    @Transactional
    public void removeModuleFromPlan(Long planId, Long moduleId) {
        if (!planRepository.existsById(planId)) {
            throw new ResourceNotFoundException("Plan not found: " + planId);
        }
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module not found: " + moduleId);
        }

        planModuleRepository.deleteByPlanIdAndModuleId(planId, moduleId);
        log.info("Removed module {} from plan {}", moduleId, planId);
    }

    @Transactional
    public void updatePlanModules(Long planId, List<Long> moduleIds) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // Remove existing
        List<PlanModule> existing = planModuleRepository.findByPlanId(planId);
        planModuleRepository.deleteAll(existing);

        // Add new
        for (Long moduleId : moduleIds) {
            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

            PlanModule planModule = PlanModule.builder()
                    .plan(plan)
                    .module(module)
                    .build();
            planModuleRepository.save(planModule);
        }

        log.info("Updated modules for plan {}: {}", plan.getCode(), moduleIds);
    }

    private PlanDTO toPlanDTO(Plan plan) {
        List<String> moduleCodes = planModuleRepository.findByPlanId(plan.getId())
                .stream()
                .map(pm -> pm.getModule().getCode())
                .collect(Collectors.toList());

        return PlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .code(plan.getCode())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxUsers(plan.getMaxUsers())
                .maxProducts(plan.getMaxProducts())
                .maxCustomers(plan.getMaxCustomers())
                .isActive(plan.getIsActive())
                .displayOrder(plan.getDisplayOrder())
                .moduleCodes(moduleCodes)
                .build();
    }
}
