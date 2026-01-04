-- Seed Data for User Service
-- Version: 1.0.0

-- Insert System Roles
INSERT INTO roles (id, tenant_id, name, code, description, is_system_role, is_active) VALUES
(1, NULL, 'Super Admin', 'SUPER_ADMIN', 'Platform-level administrator with full access', true, true),
(2, NULL, 'Admin', 'ADMIN', 'Tenant administrator with full tenant access', true, true),
(3, NULL, 'Pharmacist', 'PHARMACIST', 'Inventory and billing management', true, true),
(4, NULL, 'Salesman', 'SALESMAN', 'Billing and customer management', true, true),
(5, NULL, 'Doctor', 'DOCTOR', 'Doctor with commission tracking access', true, true);

-- Insert Permissions
INSERT INTO permissions (module, action, code, description) VALUES
-- Dashboard
('DASHBOARD', 'VIEW', 'DASHBOARD_VIEW', 'View dashboard'),

-- User Management
('USER', 'VIEW', 'USER_VIEW', 'View users'),
('USER', 'CREATE', 'USER_CREATE', 'Create users'),
('USER', 'EDIT', 'USER_EDIT', 'Edit users'),
('USER', 'DELETE', 'USER_DELETE', 'Delete users'),

-- Role Management
('ROLE', 'VIEW', 'ROLE_VIEW', 'View roles'),
('ROLE', 'EDIT', 'ROLE_EDIT', 'Edit role permissions'),

-- Category Management
('CATEGORY', 'VIEW', 'CATEGORY_VIEW', 'View categories'),
('CATEGORY', 'CREATE', 'CATEGORY_CREATE', 'Create categories'),
('CATEGORY', 'EDIT', 'CATEGORY_EDIT', 'Edit categories'),
('CATEGORY', 'DELETE', 'CATEGORY_DELETE', 'Delete categories'),

-- Product Management
('PRODUCT', 'VIEW', 'PRODUCT_VIEW', 'View products'),
('PRODUCT', 'CREATE', 'PRODUCT_CREATE', 'Create products'),
('PRODUCT', 'EDIT', 'PRODUCT_EDIT', 'Edit products'),
('PRODUCT', 'DELETE', 'PRODUCT_DELETE', 'Delete products'),

-- Stock Management
('STOCK', 'VIEW', 'STOCK_VIEW', 'View stock'),
('STOCK', 'ENTRY', 'STOCK_ENTRY', 'Record stock entry'),
('STOCK', 'ADJUST', 'STOCK_ADJUST', 'Adjust stock'),

-- Customer Management
('CUSTOMER', 'VIEW', 'CUSTOMER_VIEW', 'View customers'),
('CUSTOMER', 'CREATE', 'CUSTOMER_CREATE', 'Create customers'),
('CUSTOMER', 'EDIT', 'CUSTOMER_EDIT', 'Edit customers'),
('CUSTOMER', 'DELETE', 'CUSTOMER_DELETE', 'Delete customers'),

-- Wallet Management
('WALLET', 'VIEW', 'WALLET_VIEW', 'View wallet'),
('WALLET', 'CREDIT', 'WALLET_CREDIT', 'Credit wallet'),
('WALLET', 'DEBIT', 'WALLET_DEBIT', 'Debit wallet'),

-- Doctor Management
('DOCTOR', 'VIEW', 'DOCTOR_VIEW', 'View doctors'),
('DOCTOR', 'CREATE', 'DOCTOR_CREATE', 'Create doctors'),
('DOCTOR', 'EDIT', 'DOCTOR_EDIT', 'Edit doctors'),
('DOCTOR', 'DELETE', 'DOCTOR_DELETE', 'Delete doctors'),
('DOCTOR', 'PAY_COMMISSION', 'DOCTOR_PAY_COMMISSION', 'Pay doctor commission'),

-- Supplier Management
('SUPPLIER', 'VIEW', 'SUPPLIER_VIEW', 'View suppliers'),
('SUPPLIER', 'CREATE', 'SUPPLIER_CREATE', 'Create suppliers'),
('SUPPLIER', 'EDIT', 'SUPPLIER_EDIT', 'Edit suppliers'),
('SUPPLIER', 'DELETE', 'SUPPLIER_DELETE', 'Delete suppliers'),
('SUPPLIER', 'PAY', 'SUPPLIER_PAY', 'Pay supplier'),

-- Billing Management
('BILLING', 'VIEW', 'BILLING_VIEW', 'View bills'),
('BILLING', 'CREATE', 'BILLING_CREATE', 'Create bills'),
('BILLING', 'EDIT', 'BILLING_EDIT', 'Edit bills'),
('BILLING', 'CANCEL', 'BILLING_CANCEL', 'Cancel bills'),
('BILLING', 'RECEIVE_PAYMENT', 'BILLING_RECEIVE_PAYMENT', 'Receive payments'),

-- Reports
('REPORT', 'SALES', 'REPORT_SALES', 'View sales reports'),
('REPORT', 'INVENTORY', 'REPORT_INVENTORY', 'View inventory reports'),
('REPORT', 'CUSTOMER', 'REPORT_CUSTOMER', 'View customer reports'),
('REPORT', 'DOCTOR', 'REPORT_DOCTOR', 'View doctor reports'),
('REPORT', 'SUPPLIER', 'REPORT_SUPPLIER', 'View supplier reports'),

-- Configuration
('CONFIG', 'VIEW', 'CONFIG_VIEW', 'View configuration'),
('CONFIG', 'EDIT', 'CONFIG_EDIT', 'Edit configuration');

-- Assign all permissions to ADMIN role (role_id = 2)
INSERT INTO role_permissions (tenant_id, role_id, permission_id)
SELECT NULL, 2, id FROM permissions;

-- Assign permissions to PHARMACIST role (role_id = 3)
INSERT INTO role_permissions (tenant_id, role_id, permission_id)
SELECT NULL, 3, id FROM permissions WHERE code IN (
    'DASHBOARD_VIEW',
    'CATEGORY_VIEW',
    'PRODUCT_VIEW', 'PRODUCT_CREATE', 'PRODUCT_EDIT',
    'STOCK_VIEW', 'STOCK_ENTRY', 'STOCK_ADJUST',
    'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_EDIT',
    'WALLET_VIEW', 'WALLET_CREDIT', 'WALLET_DEBIT',
    'DOCTOR_VIEW',
    'SUPPLIER_VIEW',
    'BILLING_VIEW', 'BILLING_CREATE', 'BILLING_EDIT', 'BILLING_RECEIVE_PAYMENT',
    'REPORT_INVENTORY'
);

-- Assign permissions to SALESMAN role (role_id = 4)
INSERT INTO role_permissions (tenant_id, role_id, permission_id)
SELECT NULL, 4, id FROM permissions WHERE code IN (
    'DASHBOARD_VIEW',
    'PRODUCT_VIEW',
    'STOCK_VIEW',
    'CUSTOMER_VIEW', 'CUSTOMER_CREATE', 'CUSTOMER_EDIT',
    'WALLET_VIEW', 'WALLET_CREDIT', 'WALLET_DEBIT',
    'DOCTOR_VIEW',
    'BILLING_VIEW', 'BILLING_CREATE', 'BILLING_EDIT', 'BILLING_RECEIVE_PAYMENT'
);

-- Assign permissions to DOCTOR role (role_id = 5)
INSERT INTO role_permissions (tenant_id, role_id, permission_id)
SELECT NULL, 5, id FROM permissions WHERE code IN (
    'DASHBOARD_VIEW'
);

-- Insert Super Admin User (password: Admin@123)
INSERT INTO users (id, name, phone, email, password_hash, is_active) VALUES
(1, 'Super Admin', '9999999999', 'admin@rxbuddy.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.i5tJlVFKkZzTHC', true);

-- Link Super Admin to platform (tenant_id = 0 represents platform level)
INSERT INTO user_tenants (user_id, tenant_id, tenant_name, role_id, is_active) VALUES
(1, 0, 'RXBuddy Platform', 1, true);
