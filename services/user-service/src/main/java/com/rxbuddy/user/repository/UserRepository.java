package com.rxbuddy.user.repository;

import com.rxbuddy.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userTenants ut LEFT JOIN FETCH ut.role " +
            "WHERE u.phone = :phone AND u.deletedAt IS NULL")
    Optional<User> findByPhoneWithTenants(@Param("phone") String phone);
}
