package com.rxbuddy.card.repository;

import com.rxbuddy.card.entity.CategoryDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDiscountRepository extends JpaRepository<CategoryDiscount, Long> {

    List<CategoryDiscount> findByTenantIdAndIsActiveTrue(Long tenantId);

    Optional<CategoryDiscount> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    void deleteByTenantIdAndCategoryId(Long tenantId, Long categoryId);
}
