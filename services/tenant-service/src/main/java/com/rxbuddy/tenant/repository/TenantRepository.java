package com.rxbuddy.tenant.repository;

import com.rxbuddy.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findByPhone(String phone);

    boolean existsByCode(String code);

    boolean existsByPhone(String phone);
}
