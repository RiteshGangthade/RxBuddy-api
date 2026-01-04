-- Tenant Service Database Schema
-- Version: 1.0.0

-- Tenants table (Pharmacy/Store information)
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,

    -- Business info
    business_name VARCHAR(255),
    gst_number VARCHAR(20),
    drug_license_number VARCHAR(50),
    pan_number VARCHAR(15),

    -- Contact
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(255),

    -- Address
    address_line VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    country VARCHAR(100) DEFAULT 'India',

    -- Settings
    currency VARCHAR(10) DEFAULT 'INR',
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    date_format VARCHAR(20) DEFAULT 'dd/MM/yyyy',
    logo_path VARCHAR(500),

    -- Status
    is_active BOOLEAN DEFAULT true,
    activated_at TIMESTAMP NULL,
    deactivated_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_tenants_code (code),
    INDEX idx_tenants_phone (phone),
    INDEX idx_tenants_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Plans table (Subscription plans)
CREATE TABLE plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),

    -- Pricing
    monthly_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    yearly_price DECIMAL(10, 2) NOT NULL DEFAULT 0,

    -- Limits
    max_users INT DEFAULT 5,
    max_products INT DEFAULT 1000,
    max_customers INT DEFAULT 5000,

    -- Features
    features JSON,

    is_active BOOLEAN DEFAULT true,
    display_order INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_plans_code (code),
    INDEX idx_plans_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Subscriptions table (Tenant to Plan mapping)
CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    -- Dates
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,

    -- Payment
    billing_cycle ENUM('MONTHLY', 'YEARLY') DEFAULT 'YEARLY',
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0,

    -- Status
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED', 'TRIAL') DEFAULT 'TRIAL',
    trial_ends_at DATE,

    -- Renewal
    auto_renew BOOLEAN DEFAULT true,
    renewed_from_id BIGINT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY (renewed_from_id) REFERENCES subscriptions(id),

    INDEX idx_subscriptions_tenant (tenant_id),
    INDEX idx_subscriptions_status (status),
    INDEX idx_subscriptions_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Modules table (Master list of all available modules)
CREATE TABLE modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    is_core BOOLEAN DEFAULT false,
    display_order INT DEFAULT 0,
    icon VARCHAR(100),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_modules_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Plan Modules (Which modules are included in each plan)
CREATE TABLE plan_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    UNIQUE KEY uk_plan_module (plan_id, module_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tenant Modules (Override per tenant - Super Admin controlled)
CREATE TABLE tenant_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,

    is_enabled BOOLEAN DEFAULT true,

    -- Audit
    enabled_by BIGINT,
    enabled_at TIMESTAMP NULL,
    disabled_by BIGINT,
    disabled_at TIMESTAMP NULL,
    notes VARCHAR(500),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tenant_module (tenant_id, module_id),

    INDEX idx_tenant_modules_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payment History
CREATE TABLE payment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,

    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',

    payment_method ENUM('ONLINE', 'CASH', 'CHEQUE', 'BANK_TRANSFER') DEFAULT 'ONLINE',
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',

    transaction_id VARCHAR(100),
    payment_gateway VARCHAR(50),
    gateway_response JSON,

    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),

    INDEX idx_payment_tenant (tenant_id),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
