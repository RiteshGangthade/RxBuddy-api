package com.rxbuddy.tenant.repository;

import com.rxbuddy.tenant.entity.PlanModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanModuleRepository extends JpaRepository<PlanModule, Long> {

    List<PlanModule> findByPlanId(Long planId);

    Optional<PlanModule> findByPlanIdAndModuleId(Long planId, Long moduleId);

    void deleteByPlanIdAndModuleId(Long planId, Long moduleId);

    boolean existsByPlanIdAndModuleId(Long planId, Long moduleId);
}
