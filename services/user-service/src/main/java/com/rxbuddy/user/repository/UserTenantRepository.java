package com.rxbuddy.user.repository;

import com.rxbuddy.user.entity.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, Long> {

    List<UserTenant> findByUserIdAndIsActiveTrue(Long userId);

    Optional<UserTenant> findByUserIdAndTenantIdAndIsActiveTrue(Long userId, Long tenantId);

    @Query("SELECT ut FROM UserTenant ut JOIN FETCH ut.role r LEFT JOIN FETCH r.rolePermissions rp " +
            "LEFT JOIN FETCH rp.permission WHERE ut.user.id = :userId AND ut.tenantId = :tenantId AND ut.isActive = true")
    Optional<UserTenant> findByUserIdAndTenantIdWithPermissions(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    List<UserTenant> findByTenantIdAndIsActiveTrue(Long tenantId);
}
