# Backend & Database Logic

## Architecture at a Glance
- **Stack:** Spring Boot 3.5, Java 21, PostgreSQL, JPA/Hibernate, Spring Security + JWT.
- **Auth model:** Users authenticated via JWT; roles are `ADMIN` and `EMPLOYEE` (granted as `ROLE_ADMIN` / `ROLE_EMPLOYEE`).
- **API shape:** Controllers expose admin and employee endpoints; services hold business logic; repositories are Spring Data JPA.
- **Error handling:** Business conflicts and missing resources surface via `ResponseStatusException` with appropriate HTTP codes.

## Data Model (Tables)
- **departments** (`Department`) — `id`, `name` (unique), `code` (unique, uppercased), `description`, timestamps.
- **users** (`User`) — `id`, `fullName`, `email` (unique, lowercased), `password` (BCrypt), `role`, `department_id` (nullable FK), `enabled`, `createdAt`.
- **assets** (`Asset`) — `id`, `name`, `category`, `serialNumber` (unique), `purchaseDate`, `purchaseCost`, `vendor`, `status` (`AVAILABLE|ASSIGNED|UNDER_MAINTENANCE|RETIRED`), `location`, `usefulLifeMonths`, `salvageValue`, `imageUrl/publicId`, `documentUrl/publicId`, `notes`, timestamps.
- **asset_assignments** (`AssetAssignment`) — `id`, `asset_id` FK, `employee_id` FK (`User` with role EMPLOYEE), `status` (`ASSIGNED|RETURNED`), `assignedAt`, `dueBackAt`, `returnedAt`, `notes`.
- **maintenance** (`Maintenance`) — `id`, `asset_id` FK, `type`, `status` (`SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED`), `scheduledFor`, `completedOn`, `cost`, `vendor`, `notes`, timestamps.
- **ai_insights** (`AiInsight`) — stores AI outputs with `useCase`, optional `asset_id`, `result`, `generatedAt` (used for risk/recommendation views).
- **Depreciation fields** — lives on `Asset` (`usefulLifeMonths`, `purchaseCost`, `purchaseDate`, `salvageValue`); calculations are done in services, not persisted per period.

## Core Business Logic by Service
### AuthService
- **register:** Rejects duplicate emails; lowercases email; encodes password; optionally links to a department; returns JWT + expiry + role + user identity.
- **authenticate:** Spring AuthenticationManager verifies credentials; issues JWT with same payload fields.

### DepartmentService
- **create/update:** Enforces unique `name` and `code` (case-insensitive). `code` uppercased, name trimmed. Raises 409 on conflict, 404 on missing during update.
- **delete:** 404 if not found; then delete.
- **listAll:** Returns all departments mapped to DTO.

### EmployeeAdminService
- **createEmployee:** Rejects duplicate emails; creates EMPLOYEE with encoded password and optional department FK.
- **updateRole:** Sets role for a given user (404 if missing).
- **resetPassword:** Re-encodes password for a user (404 if missing).
- **listEmployees:** Returns only users with role EMPLOYEE.

### AssetService
- **create/update:** Validates unique serial number (409 on conflict). Maps request fields, default status to `AVAILABLE` if absent.
- **delete:** Removes asset after deleting Cloudinary image/document (if present).
- **updateStatus:** Direct status patch (404 if asset not found).
- **uploadImage/uploadDocument:** Uploads to Cloudinary, replaces stored URLs/public IDs, cleans previous files. Returns 502 on upload failure.
- **search:** Builds JPA Specification for text search (`name`, `serialNumber`, `category`), status filter, and category filter.
- **get:** Fetch single asset (404 if missing).

### AssetAssignmentService
- **assign:**
  - Validates asset exists and is not currently `ASSIGNED`.
  - Validates employee exists and has role EMPLOYEE.
  - Creates assignment with `ASSIGNED` status, timestamps `assignedAt`, copies dueBack/notes.
  - Sets asset status to `ASSIGNED`.
- **reassign:** Only allowed when current assignment is `ASSIGNED`; closes it as `RETURNED` (timestamps `returnedAt`, sets asset to `AVAILABLE`), then calls assign() with the new request.
- **markReturned:** If already returned, no-op; otherwise sets status `RETURNED`, timestamps `returnedAt`, and flips asset status back to `AVAILABLE`.
- **search:** Specification filter by `assetId`, `employeeId`, and/or `status`.
- **requireActiveAssignment/currentEmployeeId:** Helpers for employee-facing flows (authorization checks by ownership of an active assignment).

### MaintenanceService
- **log:** Creates maintenance for an asset (default status `SCHEDULED`).
- **update:** Edits maintenance and associated asset reference.
- **delete:** 404 if missing, then delete.
- **search:** Specification filter by `assetId` and `status`.
- **totalCost:** Returns aggregate maintenance cost for a given asset (404 if asset missing).

### DashboardService
- **summary(highMaintenanceThreshold, maintenanceWindowDays):**
  - Counts assets by category and by status.
  - Finds scheduled maintenance within a rolling window (today → window end).
  - Finds high-maintenance assets via repository aggregate with a configurable cost threshold.
  - Flags near end-of-life assets using purchase date and useful life (<=15% of life or <=3 months remaining).
  - Pulls latest AI “risk” insights tied to assets.

### ReportService
- **inventory:** Basic asset snapshot (id, name, category, status, cost, purchaseDate, location).
- **maintenanceCosts:** Aggregated maintenance cost per asset.
- **depreciationSummaries:** For assets with sufficient data, delegates to `DepreciationService.summarize(id)` for straight-line schedule outputs.
- **aiRecommendations:** Returns AI insight feed (optionally filtered by `useCase`).

### Auth & Security Layers
- JWT issued by `JwtService`, attached as Bearer; Spring Security guards endpoints via `@PreAuthorize` on controllers (admin vs employee scopes).
- UserDetails is provided by `User`, granting authorities `ROLE_<role>`.

## Database Behaviors & Constraints
- UUID primary keys generated by Hibernate (`GenerationType.AUTO`).
- Unique constraints: `users.email`, `assets.serialNumber`, `departments.name`, `departments.code`.
- Timestamp handling: `@PrePersist`/`@PreUpdate` set `createdAt`/`updatedAt` for assets/departments/maintenance; assignments set defaults for `assignedAt` and `status` on insert.
- Foreign keys: users→departments (nullable), asset_assignments→assets/users, maintenance→assets, ai_insights→assets (nullable).

## Notable Error Conditions
- 409 Conflict for duplicate emails (auth/employee), serial numbers (assets), department name/code.
- 404 Not Found for missing entities on update/delete/fetch.
- 400 Bad Request for invalid asset/employee references in assignments or maintenance.
- 502 Bad Gateway for Cloudinary upload failures.
- 403 Forbidden surfaced via `requireActiveAssignment` when an employee attempts access to unowned assets.

## Computations & Derived Data
- **End-of-life detection:** Months remaining calculated from `purchaseDate` and `usefulLifeMonths`; flagged when remaining <= min(3 months, 15% of life).
- **Depreciation:** Delegated to `DepreciationService` (straight-line style) using `purchaseCost`, `salvageValue`, `usefulLifeMonths`, `purchaseDate`.
- **High maintenance:** Aggregates maintenance cost per asset and filters by threshold supplied to dashboard.

## File Storage
- Cloudinary integration for asset images/documents; public IDs stored to enable overwrite/delete on update.

## Usage Notes
- Ensure `app.cors.allowed-origins` includes the frontend origin.
- Replace secret keys (JWT, Cloudinary, Gemini) before deployment; do not commit real secrets.
- PostgreSQL URL/credentials configured in `src/main/resources/application.properties`.
