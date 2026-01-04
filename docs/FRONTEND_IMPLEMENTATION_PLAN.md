# RxBuddy Frontend Implementation Plan

## Overview
React frontend for RxBuddy Pharmacy Management System using Redux Toolkit, TypeScript, and Tailwind CSS v4.

## Tech Stack
- **React 18** with TypeScript
- **Vite** for build tooling
- **Redux Toolkit** for state management
- **React Router v6** for routing
- **Tailwind CSS v4** for styling
- **Axios** for API calls
- **Lucide React** for icons
- **clsx** for conditional classes

## Theme
- **Primary**: Teal (#0d9488 - primary-600)
- **Secondary**: Emerald (#10b981 - secondary-500)
- **Font**: Inter

## Project Structure
```
frontend/
├── src/
│   ├── api/                    # API layer
│   │   ├── axiosInstance.ts    # Axios config with interceptors
│   │   ├── authApi.ts          # Auth endpoints
│   │   ├── cardApi.ts          # Loyalty card endpoints
│   │   ├── tenantApi.ts        # Tenant management endpoints
│   │   └── userApi.ts          # User management endpoints
│   ├── app/
│   │   ├── store.ts            # Redux store configuration
│   │   └── hooks.ts            # Typed Redux hooks
│   ├── components/
│   │   ├── common/             # Reusable UI components
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── SearchInput.tsx
│   │   │   ├── ConfirmDialog.tsx
│   │   │   └── index.ts
│   │   └── layout/             # Layout components
│   │       ├── Sidebar.tsx
│   │       ├── Header.tsx
│   │       ├── MainLayout.tsx
│   │       ├── PageHeader.tsx
│   │       └── index.ts
│   ├── features/               # Redux slices by feature
│   │   ├── auth/
│   │   │   ├── authSlice.ts
│   │   │   └── authTypes.ts
│   │   ├── tenant/
│   │   │   ├── tenantSlice.ts
│   │   │   └── tenantTypes.ts
│   │   ├── card/
│   │   │   ├── cardSlice.ts
│   │   │   └── cardTypes.ts
│   │   ├── user/
│   │   │   ├── userSlice.ts
│   │   │   └── userTypes.ts
│   │   └── ui/
│   │       └── uiSlice.ts
│   ├── pages/
│   │   ├── auth/
│   │   │   └── LoginPage.tsx
│   │   └── dashboard/
│   │       └── DashboardPage.tsx
│   ├── routes/
│   │   ├── AppRoutes.tsx       # Route configuration
│   │   ├── ProtectedRoute.tsx  # Auth-required routes
│   │   ├── AdminRoute.tsx      # Role-based routes
│   │   ├── ModuleRoute.tsx     # Module-based routes
│   │   └── index.ts
│   ├── styles/
│   │   └── globals.css         # Tailwind v4 config
│   ├── types/
│   │   └── index.ts            # Shared types
│   ├── utils/
│   │   └── storage.ts          # Local storage helpers
│   ├── App.tsx
│   └── main.tsx
├── .env
├── .env.example
├── tailwind.config.js
├── postcss.config.js
├── vite.config.ts
└── package.json
```

## Implementation Phases

### Phase 1: Project Setup (COMPLETED)
- [x] Create Vite React TypeScript project
- [x] Install dependencies
- [x] Configure Tailwind CSS v4
- [x] Setup Redux store with typed hooks
- [x] Create base types

### Phase 2: Common Components (COMPLETED)
- [x] Button component with variants
- [x] Input component with label/error
- [x] Select component
- [x] Modal component
- [x] Table component
- [x] Pagination component
- [x] Badge component
- [x] Spinner component
- [x] EmptyState component
- [x] Card component
- [x] SearchInput component
- [x] ConfirmDialog component

### Phase 3: Layout Components (COMPLETED)
- [x] Sidebar with module-based navigation
- [x] Header with user menu
- [x] MainLayout wrapper
- [x] PageHeader component

### Phase 4: Authentication (COMPLETED)
- [x] Auth Redux slice with login/logout
- [x] Token refresh handling
- [x] Login page with tenant selection
- [x] Protected route wrapper

### Phase 5: Routing (COMPLETED)
- [x] Route configuration
- [x] Protected routes (auth required)
- [x] Admin routes (role-based)
- [x] Module routes (module-based access)
- [x] Dashboard page

### Phase 6: Tenant Management (TODO)
- [ ] Tenants list page
- [ ] Tenant detail page
- [ ] Create/Edit tenant form
- [ ] Module management UI

### Phase 7: Loyalty Card Module (TODO)
- [ ] Cards list page
- [ ] Card detail page
- [ ] Create card form
- [ ] Points transaction history
- [ ] Card configuration settings
- [ ] Referral management

### Phase 8: User Management (TODO)
- [ ] Users list page
- [ ] User detail page
- [ ] Create/Edit user form
- [ ] Role assignment

## API Integration
All API calls go through `axiosInstance.ts` which:
- Adds JWT token to Authorization header
- Adds tenant ID to X-Tenant-Id header
- Handles 401 errors with token refresh
- Redirects to login on auth failure

## State Management
Redux slices handle:
- **auth**: User, tokens, authentication state
- **tenant**: Tenant CRUD, modules, plans
- **card**: Loyalty cards, transactions, configuration
- **user**: User CRUD, roles, permissions
- **ui**: Sidebar state, theme, enabled modules

## Environment Variables
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=RxBuddy
VITE_APP_VERSION=1.0.0
```

## Running the Frontend
```bash
cd frontend
npm install
npm run dev    # Development server
npm run build  # Production build
```

## Next Steps
1. Create tenant management pages
2. Implement loyalty card pages
3. Add user management pages
4. Implement form validation with react-hook-form + zod
5. Add toast notifications
6. Implement search and filtering
7. Add export functionality
