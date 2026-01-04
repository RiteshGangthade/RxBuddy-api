package com.rxbuddy.card.repository;

import com.rxbuddy.card.entity.CategoryPointConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryPointConfigRepository extends JpaRepository<CategoryPointConfig, Long> {

    List<CategoryPointConfig> findByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<CategoryPointConfig> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    void deleteByTenantIdAndCategoryId(Long tenantId, Long categoryId);
}
