package com.rxbuddy.tenant.controller;

import com.rxbuddy.common.dto.ApiResponse;
import com.rxbuddy.tenant.dto.PlanDTO;
import com.rxbuddy.tenant.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanDTO>>> getAllPlans() {
        List<PlanDTO> plans = planService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanDTO>> getPlanById(@PathVariable Long id) {
        PlanDTO plan = planService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PlanDTO>> getPlanByCode(@PathVariable String code) {
        PlanDTO plan = planService.getPlanByCode(code);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @GetMapping("/{id}/modules")
    public ResponseEntity<ApiResponse<List<String>>> getPlanModules(@PathVariable Long id) {
        List<String> moduleCodes = planService.getPlanModuleCodes(id);
        return ResponseEntity.ok(ApiResponse.success(moduleCodes));
    }

    @PostMapping("/{planId}/modules/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> addModuleToPlan(
            @PathVariable Long planId,
            @PathVariable Long moduleId) {
        planService.addModuleToPlan(planId, moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module added to plan successfully", null));
    }

    @DeleteMapping("/{planId}/modules/{moduleId}")
    public ResponseEntity<ApiResponse<Void>> removeModuleFromPlan(
            @PathVariable Long planId,
            @PathVariable Long moduleId) {
        planService.removeModuleFromPlan(planId, moduleId);
        return ResponseEntity.ok(ApiResponse.success("Module removed from plan successfully", null));
    }

    @PutMapping("/{planId}/modules")
    public ResponseEntity<ApiResponse<Void>> updatePlanModules(
            @PathVariable Long planId,
            @RequestBody List<Long> moduleIds) {
        planService.updatePlanModules(planId, moduleIds);
        return ResponseEntity.ok(ApiResponse.success("Plan modules updated successfully", null));
    }
}
