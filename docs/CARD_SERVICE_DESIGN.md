# Loyalty Card Service - Design Document

## Overview

A separate microservice to handle customer loyalty cards, points earning, referrals, and redemption during billing. Fully configurable per pharmacy (tenant).

---

## Features

### 1. Loyalty Card
- Each customer gets a unique loyalty card
- Card number (manual entry now, physical card barcode in future)
- Linked to customer basic info
- Tracks: points balance, total earned, total redeemed

### 2. Points Earning
- Customer earns points on every purchase
- Points percentage configurable **per product category**
- Example config:
  - Medicines: 1%
  - General Items: 3%
  - Baby Products: 5%

### 3. Referral System
- Customer A refers Customer B (referrer_card_id in B's card)
- On B's purchase:
  - B earns normal points
  - A earns referral points (smaller %, configurable)
- Referral chain: Only 1 level (A referred B, B referred C → A doesn't earn from C)

### 4. Points Redemption
- Points can be converted to amount during billing ONLY
- Conversion rate configurable (e.g., 100 points = ₹10)
- Max redemption limit per bill (e.g., max 50% of bill)
- Cannot withdraw as cash

### 5. Pharmacy Configuration
- Enable/disable card system entirely
- Category-wise point percentages
- Referral point percentage
- Points-to-amount conversion rate
- Max redemption percentage per bill

### 6. Discount System (Non-points based)
- Flat discount on total bill
- Category-wise discount percentages
- Can be combined with points redemption

---

## Database Schema

### card_configurations (Tenant-level settings)
```sql
CREATE TABLE card_configurations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL UNIQUE,
    is_enabled BOOLEAN DEFAULT false,
    points_to_amount_rate DECIMAL(10,2) DEFAULT 0.10,  -- 1 point = ₹0.10
    max_redemption_percent DECIMAL(5,2) DEFAULT 50.00, -- Max 50% of bill
    referral_points_percent DECIMAL(5,2) DEFAULT 0.50, -- Referrer gets 0.5%
    min_points_to_redeem INT DEFAULT 100,              -- Min 100 points to redeem
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### category_point_configs (Per-category point percentages)
```sql
CREATE TABLE category_point_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(255),                        -- Denormalized
    point_percentage DECIMAL(5,2) NOT NULL,            -- e.g., 2.00 = 2%
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE KEY uk_tenant_category (tenant_id, category_id)
);
```

### loyalty_cards (Customer cards)
```sql
CREATE TABLE loyalty_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    card_number VARCHAR(50) NOT NULL,                  -- Unique card number
    customer_id BIGINT NOT NULL,

    -- Customer Info (denormalized for quick access)
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(15) NOT NULL,
    customer_email VARCHAR(255),

    -- Referral
    referrer_card_id BIGINT,                           -- Who referred this customer

    -- Points
    points_balance DECIMAL(12,2) DEFAULT 0,
    total_points_earned DECIMAL(12,2) DEFAULT 0,
    total_points_redeemed DECIMAL(12,2) DEFAULT 0,
    total_referral_points_earned DECIMAL(12,2) DEFAULT 0,

    -- Status
    is_active BOOLEAN DEFAULT true,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_transaction_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    UNIQUE KEY uk_tenant_card (tenant_id, card_number),
    UNIQUE KEY uk_tenant_customer (tenant_id, customer_id),
    FOREIGN KEY (referrer_card_id) REFERENCES loyalty_cards(id)
);
```

### point_transactions (All point movements)
```sql
CREATE TABLE point_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,

    transaction_type ENUM('EARNED', 'REDEEMED', 'REFERRAL_EARNED', 'EXPIRED', 'ADJUSTED') NOT NULL,
    points DECIMAL(12,2) NOT NULL,                     -- Positive for earn, negative for redeem
    balance_after DECIMAL(12,2) NOT NULL,

    -- Reference
    reference_type VARCHAR(50),                        -- 'BILL', 'MANUAL', 'REFERRAL'
    reference_id BIGINT,                               -- Bill ID or referral card ID

    -- For earned points
    bill_amount DECIMAL(12,2),
    category_id BIGINT,
    category_name VARCHAR(255),
    point_percentage DECIMAL(5,2),

    -- For referral
    referred_card_id BIGINT,                           -- Whose purchase earned this referral
    referred_bill_id BIGINT,

    description VARCHAR(500),
    performed_by BIGINT,                               -- User who made transaction

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_card_transactions (card_id, created_at),
    INDEX idx_tenant_transactions (tenant_id, created_at),
    FOREIGN KEY (card_id) REFERENCES loyalty_cards(id)
);
```

### category_discounts (Category-wise discounts - separate from points)
```sql
CREATE TABLE category_discounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(255),
    discount_percentage DECIMAL(5,2) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE KEY uk_tenant_category_discount (tenant_id, category_id)
);
```

---

## API Endpoints

### Configuration APIs (Tenant Admin)
```
GET    /api/v1/card-config                    - Get card configuration
PUT    /api/v1/card-config                    - Update card configuration
POST   /api/v1/card-config/enable             - Enable card system
POST   /api/v1/card-config/disable            - Disable card system

GET    /api/v1/card-config/categories         - Get category point configs
POST   /api/v1/card-config/categories         - Add/Update category config
DELETE /api/v1/card-config/categories/{id}    - Remove category config

GET    /api/v1/card-config/discounts          - Get category discounts
POST   /api/v1/card-config/discounts          - Add/Update discount
DELETE /api/v1/card-config/discounts/{id}     - Remove discount
```

### Card Management APIs
```
GET    /api/v1/cards                          - List all cards (paginated)
POST   /api/v1/cards                          - Issue new card
GET    /api/v1/cards/{id}                     - Get card details
GET    /api/v1/cards/by-number/{cardNumber}   - Get card by number
GET    /api/v1/cards/by-customer/{customerId} - Get card by customer
PUT    /api/v1/cards/{id}                     - Update card
DELETE /api/v1/cards/{id}                     - Deactivate card

POST   /api/v1/cards/{id}/link-referrer       - Link referrer to card
GET    /api/v1/cards/{id}/referrals           - Get cards referred by this card
```

### Points & Transactions APIs
```
GET    /api/v1/cards/{id}/transactions        - Get transaction history
GET    /api/v1/cards/{id}/balance             - Get current balance

# Internal APIs (called by Billing Service)
POST   /api/v1/internal/cards/earn-points     - Earn points from purchase
POST   /api/v1/internal/cards/redeem-points   - Redeem points during billing
POST   /api/v1/internal/cards/reverse-points  - Reverse points (bill cancelled)

# Calculation API (called during billing)
POST   /api/v1/internal/cards/calculate       - Calculate points for bill items
```

---

## Flow: Points Earning on Purchase

```
Billing Service                           Card Service
     │                                         │
     │─── POST /internal/cards/earn-points ───►│
     │    {                                    │
     │      cardNumber: "CARD001",             │
     │      billId: 123,                       │
     │      billAmount: 1000,                  │
     │      items: [                           │
     │        {categoryId: 1, amount: 500},    │
     │        {categoryId: 2, amount: 500}     │
     │      ]                                  │
     │    }                                    │
     │                                         │
     │                                         │── Calculate points per category
     │                                         │── Add points to card
     │                                         │── Create transaction record
     │                                         │── Check if card has referrer
     │                                         │── Add referral points to referrer
     │                                         │
     │◄── Response ────────────────────────────│
     │    {                                    │
     │      pointsEarned: 25,                  │
     │      newBalance: 125,                   │
     │      referrerPointsEarned: 5            │
     │    }                                    │
```

---

## Flow: Points Redemption during Billing

```
Frontend (POS)                    Billing Service                    Card Service
     │                                  │                                  │
     │── Scan Card ────────────────────►│                                  │
     │                                  │── GET /cards/by-number ─────────►│
     │                                  │◄── Card details + balance ───────│
     │◄── Show balance ─────────────────│                                  │
     │                                  │                                  │
     │── Apply 50 points ──────────────►│                                  │
     │                                  │── POST /internal/cards/redeem ──►│
     │                                  │    {cardId, points: 50, billId}  │
     │                                  │                                  │
     │                                  │                                  │── Validate min points
     │                                  │                                  │── Check max redemption %
     │                                  │                                  │── Deduct points
     │                                  │                                  │── Create transaction
     │                                  │                                  │
     │                                  │◄── {amountDeducted: 5.00} ───────│
     │                                  │                                  │
     │◄── Bill updated ─────────────────│                                  │
     │    (total reduced by ₹5)         │                                  │
```

---

## Service Dependencies

```
                    ┌─────────────────┐
                    │  Card Service   │
                    │    (:8089)      │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
       ┌───────────┐  ┌───────────┐  ┌───────────┐
       │  Billing  │  │ Customer  │  │ Inventory │
       │  Service  │  │  Service  │  │  Service  │
       └───────────┘  └───────────┘  └───────────┘
       (Earn/Redeem)  (Customer ID)  (Category info)
```

---

## Configuration Example (Per Pharmacy)

```json
{
  "tenantId": 1,
  "isEnabled": true,
  "pointsToAmountRate": 0.10,
  "maxRedemptionPercent": 50,
  "referralPointsPercent": 0.5,
  "minPointsToRedeem": 100,
  "categoryPointConfigs": [
    {"categoryId": 1, "categoryName": "Medicines", "pointPercentage": 1.0},
    {"categoryId": 2, "categoryName": "General", "pointPercentage": 3.0},
    {"categoryId": 3, "categoryName": "Baby Care", "pointPercentage": 5.0},
    {"categoryId": 4, "categoryName": "Cosmetics", "pointPercentage": 2.0}
  ],
  "categoryDiscounts": [
    {"categoryId": 1, "categoryName": "Medicines", "discountPercentage": 5.0},
    {"categoryId": 3, "categoryName": "Baby Care", "discountPercentage": 10.0}
  ]
}
```

---

## Future Enhancements

1. **Physical Card Integration**
   - Barcode/QR code scanning
   - Card printing with customer details

2. **Points Expiry**
   - Points expire after X months
   - Scheduled job to expire points

3. **Tier System**
   - Bronze, Silver, Gold based on total purchases
   - Higher tiers get higher point percentages

4. **Promotions**
   - Double points weekends
   - Category-specific bonus points

5. **Analytics**
   - Top customers by points
   - Redemption rate
   - Referral effectiveness

---

## Implementation Priority

**Phase 1 (Current)**
- Card entity with customer info
- Basic points earning on purchase
- Points redemption during billing
- Tenant configuration

**Phase 2**
- Referral system
- Category-wise point configuration

**Phase 3**
- Category discounts
- Transaction history & reports

**Phase 4 (Future)**
- Physical card integration
- Points expiry
- Tier system
