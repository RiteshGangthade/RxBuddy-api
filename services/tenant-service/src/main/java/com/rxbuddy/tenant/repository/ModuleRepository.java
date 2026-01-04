package com.rxbuddy.tenant.repository;

import com.rxbuddy.tenant.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByCode(String code);

    List<Module> findAllByOrderByDisplayOrder();

    List<Module> findByIsCoreTrueOrderByDisplayOrder();

    @Query("SELECT m FROM Module m WHERE m.isCore = true " +
           "OR m.id IN (SELECT tm.module.id FROM TenantModule tm WHERE tm.tenant.id = :tenantId AND tm.isEnabled = true) " +
           "OR (m.id IN (SELECT pm.module.id FROM PlanModule pm JOIN Subscription s ON s.plan.id = pm.plan.id " +
           "WHERE s.tenant.id = :tenantId AND s.status = 'ACTIVE') " +
           "AND m.id NOT IN (SELECT tm.module.id FROM TenantModule tm WHERE tm.tenant.id = :tenantId AND tm.isEnabled = false)) " +
           "ORDER BY m.displayOrder")
    List<Module> findEnabledModulesForTenant(@Param("tenantId") Long tenantId);

    boolean existsByCode(String code);
}
