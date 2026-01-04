-- Seed data for Tenant Service
-- Version: 1.0.0

-- Insert Modules (Master list)
INSERT INTO modules (code, name, description, is_core, display_order, icon) VALUES
('INVENTORY', 'Inventory Management', 'Products, Categories, Stock management', true, 1, 'box'),
('BILLING', 'Billing & POS', 'Create bills, receive payments, invoices', true, 2, 'receipt'),
('CUSTOMER', 'Customer Management', 'Manage customer profiles and history', true, 3, 'users'),
('USER_MANAGEMENT', 'User Management', 'Staff management, roles, permissions', true, 4, 'user-cog'),
('LOYALTY_CARD', 'Loyalty Card System', 'Points earning, referrals, redemption', false, 5, 'credit-card'),
('DOCTOR', 'Doctor Management', 'Doctors and commission tracking', false, 6, 'stethoscope'),
('SUPPLIER', 'Supplier Management', 'Suppliers, purchases, ledger', false, 7, 'truck'),
('REPORTS', 'Reports & Analytics', 'Sales, inventory, customer reports', false, 8, 'chart-bar'),
('NOTIFICATIONS', 'Notifications', 'SMS and Email notifications', false, 9, 'bell');

-- Insert Plans
INSERT INTO plans (code, name, description, monthly_price, yearly_price, max_users, max_products, max_customers, display_order) VALUES
('BASIC', 'Basic Plan', 'Perfect for small pharmacies just getting started', 599.00, 5999.00, 3, 500, 2000, 1),
('PRO', 'Pro Plan', 'For growing pharmacies with more features', 1199.00, 11999.00, 10, 5000, 10000, 2),
('ENTERPRISE', 'Enterprise Plan', 'Complete solution for large pharmacy chains', 2399.00, 23999.00, -1, -1, -1, 3);

-- Link modules to Basic Plan (Core modules only)
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM plans p, modules m
WHERE p.code = 'BASIC' AND m.code IN ('INVENTORY', 'BILLING', 'CUSTOMER', 'USER_MANAGEMENT');

-- Link modules to Pro Plan (Core + some premium)
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM plans p, modules m
WHERE p.code = 'PRO' AND m.code IN ('INVENTORY', 'BILLING', 'CUSTOMER', 'USER_MANAGEMENT', 'LOYALTY_CARD', 'DOCTOR', 'REPORTS');

-- Link all modules to Enterprise Plan
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM plans p, modules m
WHERE p.code = 'ENTERPRISE';

-- Insert Demo Tenant
INSERT INTO tenants (name, code, business_name, phone, email, address_line, city, state, pincode) VALUES
('Demo Pharmacy', 'DEMO001', 'Demo Medical Store', '9999999999', 'demo@rxbuddy.com', '123 MG Road', 'Mumbai', 'Maharashtra', '400001');

-- Create subscription for demo tenant (14 day trial with Pro plan)
INSERT INTO subscriptions (tenant_id, plan_id, start_date, end_date, billing_cycle, status, trial_ends_at)
SELECT t.id, p.id, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'YEARLY', 'TRIAL', DATE_ADD(CURDATE(), INTERVAL 14 DAY)
FROM tenants t, plans p
WHERE t.code = 'DEMO001' AND p.code = 'PRO';
