-- Card Service Database Schema
-- Version: 1.0.0

-- Card Configuration (Tenant-level settings)
CREATE TABLE card_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,

    is_enabled BOOLEAN DEFAULT false,

    -- Points conversion
    points_to_amount_rate DECIMAL(10, 2) DEFAULT 0.10,   -- 1 point = ₹0.10
    max_redemption_percent DECIMAL(5, 2) DEFAULT 50.00,  -- Max 50% of bill
    min_points_to_redeem INT DEFAULT 100,                 -- Min 100 points to redeem

    -- Referral settings
    referral_points_percent DECIMAL(5, 2) DEFAULT 0.50,  -- Referrer gets 0.5%
    referral_enabled BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_card_config_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Category Point Configuration (Per-category point percentages)
CREATE TABLE category_point_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(255),

    point_percentage DECIMAL(5, 2) NOT NULL DEFAULT 1.00,  -- 1% default
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_tenant_category (tenant_id, category_id),
    INDEX idx_category_point_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Loyalty Cards (Customer cards)
CREATE TABLE loyalty_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    card_number VARCHAR(50) NOT NULL,
    customer_id BIGINT NOT NULL,

    -- Customer Info (denormalized for quick access)
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(15) NOT NULL,
    customer_email VARCHAR(255),

    -- Referral (who referred this customer)
    referrer_card_id BIGINT,

    -- Points Balance
    points_balance DECIMAL(12, 2) DEFAULT 0,
    total_points_earned DECIMAL(12, 2) DEFAULT 0,
    total_points_redeemed DECIMAL(12, 2) DEFAULT 0,
    total_referral_points_earned DECIMAL(12, 2) DEFAULT 0,

    -- Status
    is_active BOOLEAN DEFAULT true,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_tenant_card (tenant_id, card_number),
    UNIQUE KEY uk_tenant_customer (tenant_id, customer_id),
    FOREIGN KEY (referrer_card_id) REFERENCES loyalty_cards(id),
    INDEX idx_loyalty_card_tenant (tenant_id),
    INDEX idx_loyalty_card_customer (customer_id),
    INDEX idx_loyalty_card_phone (customer_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Point Transactions (All point movements)
CREATE TABLE point_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,

    transaction_type ENUM('EARNED', 'REDEEMED', 'REFERRAL_EARNED', 'EXPIRED', 'ADJUSTED') NOT NULL,
    points DECIMAL(12, 2) NOT NULL,
    balance_after DECIMAL(12, 2) NOT NULL,

    -- Reference
    reference_type VARCHAR(50),              -- 'BILL', 'MANUAL', 'REFERRAL'
    reference_id BIGINT,                     -- Bill ID

    -- For earned points
    bill_amount DECIMAL(12, 2),
    category_id BIGINT,
    category_name VARCHAR(255),
    point_percentage DECIMAL(5, 2),

    -- For referral earned
    referred_card_id BIGINT,                 -- Whose purchase earned this referral
    referred_bill_id BIGINT,

    description VARCHAR(500),
    performed_by BIGINT,                     -- User who made transaction

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (card_id) REFERENCES loyalty_cards(id),
    INDEX idx_transaction_card (card_id, created_at),
    INDEX idx_transaction_tenant (tenant_id, created_at),
    INDEX idx_transaction_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Category Discounts (Non-points based discounts)
CREATE TABLE category_discounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(255),

    discount_percentage DECIMAL(5, 2) NOT NULL,
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_tenant_category_discount (tenant_id, category_id),
    INDEX idx_category_discount_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
