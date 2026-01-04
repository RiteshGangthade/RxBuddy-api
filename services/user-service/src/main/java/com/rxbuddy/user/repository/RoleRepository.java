package com.rxbuddy.user.repository;

import com.rxbuddy.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCodeAndTenantIdIsNull(String code);

    Optional<Role> findByCodeAndTenantId(String code, Long tenantId);

    List<Role> findByTenantIdIsNullOrTenantId(Long tenantId);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission " +
            "WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);
}
