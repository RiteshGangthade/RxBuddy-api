package com.rxbuddy.card.repository;

import com.rxbuddy.card.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Page<PointTransaction> findByCardIdOrderByCreatedAtDesc(Long cardId, Pageable pageable);

    Page<PointTransaction> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Optional<PointTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
