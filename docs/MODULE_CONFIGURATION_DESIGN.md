# Module Configuration System - Design Document

## Overview

Super Admin can enable/disable modules for each tenant. This allows:
- Feature gating based on subscription plans
- Gradual feature rollout
- Custom packages for different pharmacies

---

## Available Modules

| Module Code | Module Name | Description |
|-------------|-------------|-------------|
| `INVENTORY` | Inventory Management | Products, Categories, Stock |
| `BILLING` | Billing & POS | Bills, Payments, Invoices |
| `CUSTOMER` | Customer Management | Customer profiles |
| `LOYALTY_CARD` | Loyalty Card System | Points, Referrals, Redemption |
| `DOCTOR` | Doctor Management | Doctors, Commissions |
| `SUPPLIER` | Supplier Management | Suppliers, Purchases, Ledger |
| `REPORTS` | Reports & Analytics | Sales, Inventory, Customer reports |
| `USER_MANAGEMENT` | User Management | Staff, Roles, Permissions |
| `NOTIFICATIONS` | Notifications | SMS, Email alerts |

---

## Database Schema

### modules (Master list of all modules)
```sql
CREATE TABLE modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_core BOOLEAN DEFAULT false,          -- Core modules can't be disabled
    display_order INT DEFAULT 0,
    icon VARCHAR(100),                       -- Icon name for UI
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed data
INSERT INTO modules (code, name, description, is_core, display_order) VALUES
('INVENTORY', 'Inventory Management', 'Products, Categories, Stock management', true, 1),
('BILLING', 'Billing & POS', 'Create bills, receive payments', true, 2),
('CUSTOMER', 'Customer Management', 'Manage customer profiles', true, 3),
('LOYALTY_CARD', 'Loyalty Card System', 'Points earning, referrals, redemption', false, 4),
('DOCTOR', 'Doctor Management', 'Doctors and commission tracking', false, 5),
('SUPPLIER', 'Supplier Management', 'Suppliers, purchases, payments', false, 6),
('REPORTS', 'Reports & Analytics', 'Sales, inventory, customer reports', false, 7),
('USER_MANAGEMENT', 'User Management', 'Staff management, roles, permissions', true, 8),
('NOTIFICATIONS', 'Notifications', 'SMS and Email notifications', false, 9);
```

### plan_modules (Which modules are included in each plan)
```sql
CREATE TABLE plan_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    UNIQUE KEY uk_plan_module (plan_id, module_id)
);

-- Example: Basic plan has limited modules
INSERT INTO plan_modules (plan_id, module_id)
SELECT 1, id FROM modules WHERE code IN ('INVENTORY', 'BILLING', 'CUSTOMER', 'USER_MANAGEMENT');

-- Pro plan has more
INSERT INTO plan_modules (plan_id, module_id)
SELECT 2, id FROM modules WHERE code IN ('INVENTORY', 'BILLING', 'CUSTOMER', 'USER_MANAGEMENT', 'LOYALTY_CARD', 'DOCTOR', 'REPORTS');

-- Enterprise has all
INSERT INTO plan_modules (plan_id, module_id)
SELECT 3, id FROM modules;
```

### tenant_modules (Override per tenant - Super Admin controlled)
```sql
CREATE TABLE tenant_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    enabled_by BIGINT,                       -- Super Admin who enabled
    enabled_at TIMESTAMP,
    disabled_by BIGINT,                      -- Super Admin who disabled
    disabled_at TIMESTAMP,
    notes VARCHAR(500),                      -- Reason for enable/disable
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tenant_module (tenant_id, module_id)
);
```

---

## Module Access Logic

```
When checking if Tenant X can access Module Y:

1. Check if module is CORE → Always allowed
2. Check tenant_modules table for override
   - If found and enabled → Allowed
   - If found and disabled → Blocked
3. If no override, check plan_modules
   - If module in tenant's plan → Allowed
   - If not in plan → Blocked
```

### SQL Query Example:
```sql
-- Get all enabled modules for a tenant
SELECT m.* FROM modules m
WHERE m.is_core = true
   OR m.id IN (
       -- Override enabled
       SELECT tm.module_id FROM tenant_modules tm
       WHERE tm.tenant_id = ? AND tm.is_enabled = true
   )
   OR (
       -- In plan and not overridden to disabled
       m.id IN (
           SELECT pm.module_id FROM plan_modules pm
           JOIN subscriptions s ON s.plan_id = pm.plan_id
           WHERE s.tenant_id = ? AND s.status = 'ACTIVE'
       )
       AND m.id NOT IN (
           SELECT tm.module_id FROM tenant_modules tm
           WHERE tm.tenant_id = ? AND tm.is_enabled = false
       )
   );
```

---

## API Endpoints

### Super Admin APIs (Platform level)

```
# Module Master
GET    /api/v1/admin/modules                     - List all modules
POST   /api/v1/admin/modules                     - Create new module
PUT    /api/v1/admin/modules/{id}                - Update module
DELETE /api/v1/admin/modules/{id}                - Delete module (if not in use)

# Plan-Module Mapping
GET    /api/v1/admin/plans/{planId}/modules      - Get modules in plan
PUT    /api/v1/admin/plans/{planId}/modules      - Update plan modules
POST   /api/v1/admin/plans/{planId}/modules/{moduleId}    - Add module to plan
DELETE /api/v1/admin/plans/{planId}/modules/{moduleId}    - Remove from plan

# Tenant-Module Override
GET    /api/v1/admin/tenants/{tenantId}/modules  - Get tenant's module access
PUT    /api/v1/admin/tenants/{tenantId}/modules  - Update tenant modules
POST   /api/v1/admin/tenants/{tenantId}/modules/{moduleId}/enable   - Enable module
POST   /api/v1/admin/tenants/{tenantId}/modules/{moduleId}/disable  - Disable module
```

### Tenant APIs (For checking access)

```
# Get enabled modules for current tenant
GET    /api/v1/modules                           - List enabled modules
GET    /api/v1/modules/{code}/enabled            - Check if specific module enabled
```

---

## Response Examples

### GET /api/v1/admin/tenants/{tenantId}/modules
```json
{
  "success": true,
  "data": {
    "tenantId": 1,
    "tenantName": "ABC Pharmacy",
    "planName": "Pro",
    "modules": [
      {
        "code": "INVENTORY",
        "name": "Inventory Management",
        "isCore": true,
        "isEnabled": true,
        "source": "CORE"
      },
      {
        "code": "BILLING",
        "name": "Billing & POS",
        "isCore": true,
        "isEnabled": true,
        "source": "CORE"
      },
      {
        "code": "LOYALTY_CARD",
        "name": "Loyalty Card System",
        "isCore": false,
        "isEnabled": true,
        "source": "PLAN"
      },
      {
        "code": "SUPPLIER",
        "name": "Supplier Management",
        "isCore": false,
        "isEnabled": false,
        "source": "OVERRIDE",
        "disabledAt": "2024-12-20T10:00:00",
        "disabledBy": "Super Admin",
        "notes": "Disabled as per request"
      },
      {
        "code": "NOTIFICATIONS",
        "name": "Notifications",
        "isCore": false,
        "isEnabled": true,
        "source": "OVERRIDE",
        "enabledAt": "2024-12-15T10:00:00",
        "enabledBy": "Super Admin",
        "notes": "Special add-on enabled"
      }
    ]
  }
}
```

### GET /api/v1/modules (Tenant's view)
```json
{
  "success": true,
  "data": [
    {"code": "INVENTORY", "name": "Inventory Management", "icon": "box"},
    {"code": "BILLING", "name": "Billing & POS", "icon": "receipt"},
    {"code": "CUSTOMER", "name": "Customer Management", "icon": "users"},
    {"code": "LOYALTY_CARD", "name": "Loyalty Card System", "icon": "card"},
    {"code": "DOCTOR", "name": "Doctor Management", "icon": "stethoscope"},
    {"code": "REPORTS", "name": "Reports & Analytics", "icon": "chart"}
  ]
}
```

---

## Frontend Integration

### Sidebar Navigation
```typescript
// Fetch enabled modules on login
const enabledModules = await api.get('/modules');

// Show only enabled modules in sidebar
const sidebarItems = [
  { code: 'DASHBOARD', name: 'Dashboard', icon: 'home', alwaysShow: true },
  { code: 'INVENTORY', name: 'Inventory', icon: 'box' },
  { code: 'BILLING', name: 'Billing', icon: 'receipt' },
  { code: 'CUSTOMER', name: 'Customers', icon: 'users' },
  { code: 'LOYALTY_CARD', name: 'Loyalty Cards', icon: 'card' },
  // ... more
].filter(item => item.alwaysShow || enabledModules.includes(item.code));
```

### API Gateway Route Protection
```yaml
# In API Gateway, check module access before routing
filters:
  - name: ModuleAccessFilter
    args:
      moduleCode: LOYALTY_CARD
      path: /api/v1/cards/**
```

---

## Plan Examples

### Basic Plan (₹5,999/year)
- ✅ Inventory
- ✅ Billing
- ✅ Customer
- ✅ User Management
- ❌ Loyalty Card
- ❌ Doctor
- ❌ Supplier
- ❌ Reports
- ❌ Notifications

### Pro Plan (₹11,999/year)
- ✅ Inventory
- ✅ Billing
- ✅ Customer
- ✅ User Management
- ✅ Loyalty Card
- ✅ Doctor
- ❌ Supplier
- ✅ Reports
- ❌ Notifications

### Enterprise Plan (₹23,999/year)
- ✅ All Modules

### Custom Override Examples
- Tenant on Basic plan but Super Admin enabled "Loyalty Card" as special add-on
- Tenant on Enterprise but "Notifications" disabled (don't want SMS costs)

---

## Implementation Notes

1. **Cache module access** - Don't query DB on every request
2. **Include in JWT** - Add enabled modules to JWT payload
3. **Frontend + Backend validation** - Both should check
4. **Audit trail** - Log all enable/disable actions
5. **Grace period** - When disabling, maybe give 7 days notice
