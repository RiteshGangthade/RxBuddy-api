package com.rxbuddy.card.repository;

import com.rxbuddy.card.entity.CardConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardConfigurationRepository extends JpaRepository<CardConfiguration, Long> {

    Optional<CardConfiguration> findByTenantId(Long tenantId);

    boolean existsByTenantId(Long tenantId);
}
