package com.rxbuddy.tenant.repository;

import com.rxbuddy.tenant.entity.TenantModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

    List<TenantModule> findByTenantId(Long tenantId);

    Optional<TenantModule> findByTenantIdAndModuleId(Long tenantId, Long moduleId);

    Optional<TenantModule> findByTenantIdAndModuleCode(Long tenantId, String moduleCode);

    boolean existsByTenantIdAndModuleId(Long tenantId, Long moduleId);
}
