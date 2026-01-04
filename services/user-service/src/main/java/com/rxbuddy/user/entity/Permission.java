package com.rxbuddy.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String module; // INVENTORY, BILLING, CUSTOMER, etc.

    @Column(nullable = false, length = 100)
    private String action; // VIEW, CREATE, EDIT, DELETE

    @Column(nullable = false, unique = true, length = 100)
    private String code; // INVENTORY_VIEW, BILLING_CREATE, etc.

    @Column(length = 500)
    private String description;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
