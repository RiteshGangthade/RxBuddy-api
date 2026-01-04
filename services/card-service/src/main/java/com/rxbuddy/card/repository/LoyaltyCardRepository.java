package com.rxbuddy.card.repository;

import com.rxbuddy.card.entity.LoyaltyCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyCardRepository extends JpaRepository<LoyaltyCard, Long> {

    Page<LoyaltyCard> findByTenantId(Long tenantId, Pageable pageable);

    Optional<LoyaltyCard> findByTenantIdAndCardNumber(Long tenantId, String cardNumber);

    Optional<LoyaltyCard> findByTenantIdAndCustomerId(Long tenantId, Long customerId);

    Optional<LoyaltyCard> findByTenantIdAndCustomerPhone(Long tenantId, String customerPhone);

    boolean existsByTenantIdAndCardNumber(Long tenantId, String cardNumber);

    boolean existsByTenantIdAndCustomerId(Long tenantId, Long customerId);

    @Query("SELECT c FROM LoyaltyCard c WHERE c.tenantId = :tenantId AND c.referrerCard.id = :referrerCardId")
    List<LoyaltyCard> findReferrals(@Param("tenantId") Long tenantId, @Param("referrerCardId") Long referrerCardId);

    @Query("SELECT c FROM LoyaltyCard c WHERE c.tenantId = :tenantId " +
           "AND (c.customerName LIKE %:search% OR c.customerPhone LIKE %:search% OR c.cardNumber LIKE %:search%)")
    Page<LoyaltyCard> searchCards(@Param("tenantId") Long tenantId, @Param("search") String search, Pageable pageable);
}
