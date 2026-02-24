# BACKEND.md

## SECTION 1: SYSTEM OVERVIEW

### High-Level Backend Architecture
The AI-Powered Asset Lifecycle Management System is designed for enterprise-grade asset tracking, maintenance, and optimization. The backend leverages Spring Boot for robust RESTful APIs, PostgreSQL for reliable data storage, JWT for secure authentication, Gemini AI for intelligent insights, and Cloudinary for scalable file storage.

#### Layered Architecture
- **Controller Layer:** Handles HTTP requests, maps endpoints, and delegates to services. Ensures input validation and response formatting.
- **Service Layer:** Contains business logic, orchestrates workflows, enforces validation, and manages transactions.
- **Repository Layer:** Uses Spring Data JPA for database access, entity mapping, and query abstraction.
- **Security Layer:** Implements JWT authentication, role-based access control, and password encryption.
- **AI Integration Layer:** Interfaces with Gemini API, builds prompts, parses responses, and stores AI insights.
- **File Storage Layer:** Integrates with Cloudinary for secure image/document upload, retrieval, and deletion.

#### Request Flow
1. Client sends HTTP request to Controller.
2. Controller validates input and forwards to Service.
3. Service executes business logic, interacts with Repository, AI, or File Storage as needed.
4. Repository persists/retrieves data from PostgreSQL.
5. Security Layer authenticates and authorizes requests.
6. AI Layer processes asset data and stores insights.
7. File Storage Layer manages uploads/downloads.
8. Controller returns structured JSON response.

---

## SECTION 2: SECURITY ARCHITECTURE

### JWT Authentication Flow
- User submits credentials to `/api/auth/login`.
- Backend validates credentials, generates JWT token.
- Token includes user identity, role, expiry.
- Token is returned to client and used in Authorization header for subsequent requests.

#### Token Generation & Validation
- JWT signed with secret key.
- Token validated on each request via Security Filter.
- Expired or invalid tokens rejected with 401 Unauthorized.

#### Role-Based Authorization
- `ADMIN`: Full access to all endpoints.
- `EMPLOYEE`: Restricted access to own assets, maintenance requests, and AI insights.
- Endpoints protected via `@PreAuthorize` annotations.

#### BCrypt Password Encryption
- Passwords hashed using BCrypt before storage.
- Passwords never returned in responses.

#### Security Filter Flow
1. Extract JWT from Authorization header.
2. Validate token signature and expiry.
3. Load user details and roles.
4. Grant/deny access based on endpoint and role.

#### Sample JWT Payload
```json
{
  "sub": "user@example.com",
  "role": "ADMIN",
  "exp": 1745678900,
  "userId": "c1a2b3d4-e5f6-7890-abcd-1234567890ab"
}
```

#### Authentication Request & Response
**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "role": "ADMIN",
  "user": {
    "id": "c1a2b3d4-e5f6-7890-abcd-1234567890ab",
    "email": "user@example.com",
    "fullName": "John Doe"
  }
}
```

---

## SECTION 3: DATABASE DESIGN

### Table: departments
| Field         | Data Type    | Constraints                |
|--------------|-------------|----------------------------|
| id           | UUID         | PK, NOT NULL               |
| name         | VARCHAR(100) | UNIQUE, NOT NULL           |
| code         | VARCHAR(20)  | UNIQUE, NOT NULL, UPPERCASE|
| description  | TEXT         |                            |
| created_at   | TIMESTAMP    | NOT NULL                   |
| updated_at   | TIMESTAMP    |                            |

**Indexes:**
- Unique index on `name`
- Unique index on `code`

### Table: employees
| Field         | Data Type    | Constraints                |
|--------------|-------------|----------------------------|
| id           | UUID         | PK, NOT NULL               |
| full_name    | VARCHAR(100) | NOT NULL                   |
| email        | VARCHAR(100) | UNIQUE, NOT NULL, LOWERCASE|
| password     | VARCHAR(255) | NOT NULL                   |
| role         | VARCHAR(20)  | CHECK (ADMIN, EMPLOYEE)    |
| department_id| UUID         | FK → departments.id        |
| enabled      | BOOLEAN      | DEFAULT TRUE               |
| created_at   | TIMESTAMP    | NOT NULL                   |

**Indexes:**
- Unique index on `email`
- Index on `department_id`

### Table: assets
| Field           | Data Type    | Constraints                |
|----------------|-------------|----------------------------|
| id             | UUID         | PK, NOT NULL               |
| name           | VARCHAR(100) | NOT NULL                   |
| category       | VARCHAR(50)  | NOT NULL                   |
| serial_number  | VARCHAR(50)  | UNIQUE, NOT NULL           |
| purchase_date  | DATE         | NOT NULL                   |
| purchase_cost  | DECIMAL(12,2)| NOT NULL                   |
| vendor         | VARCHAR(100) |                            |
| status         | VARCHAR(20)  | CHECK (AVAILABLE, ASSIGNED, UNDER_MAINTENANCE, RETIRED) |
| location       | VARCHAR(100) |                            |
| useful_life    | INT          | NOT NULL                   |
| salvage_value  | DECIMAL(12,2)|                            |
| image_url      | VARCHAR(255) |                            |
| document_url   | VARCHAR(255) |                            |
| notes          | TEXT         |                            |
| created_at     | TIMESTAMP    | NOT NULL                   |
| updated_at     | TIMESTAMP    |                            |

**Indexes:**
- Unique index on `serial_number`
- Index on `category`

### Table: asset_assignments
| Field         | Data Type    | Constraints                |
|--------------|-------------|----------------------------|
| id           | UUID         | PK, NOT NULL               |
| asset_id     | UUID         | FK → assets.id, NOT NULL   |
| employee_id  | UUID         | FK → employees.id, NOT NULL|
| status       | VARCHAR(20)  | CHECK (ASSIGNED, RETURNED) |
| assigned_at  | TIMESTAMP    | NOT NULL                   |
| due_back_at  | TIMESTAMP    |                            |
| returned_at  | TIMESTAMP    |                            |
| notes        | TEXT         |                            |

**Indexes:**
- Index on `asset_id`
- Index on `employee_id`

### Table: maintenance_records
| Field         | Data Type    | Constraints                |
|--------------|-------------|----------------------------|
| id           | UUID         | PK, NOT NULL               |
| asset_id     | UUID         | FK → assets.id, NOT NULL   |
| type         | VARCHAR(50)  | NOT NULL                   |
| status       | VARCHAR(20)  | CHECK (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED) |
| scheduled_for| TIMESTAMP    | NOT NULL                   |
| completed_on | TIMESTAMP    |                            |
| cost         | DECIMAL(12,2)|                            |
| vendor       | VARCHAR(100) |                            |
| notes        | TEXT         |                            |
| created_at   | TIMESTAMP    | NOT NULL                   |
| updated_at   | TIMESTAMP    |                            |

**Indexes:**
- Index on `asset_id`
- Index on `status`

### Table: ai_insights
| Field         | Data Type    | Constraints                |
|--------------|-------------|----------------------------|
| id           | UUID         | PK, NOT NULL               |
| asset_id     | UUID         | FK → assets.id             |
| use_case     | VARCHAR(50)  | NOT NULL                   |
| result       | JSONB        | NOT NULL                   |
| generated_at | TIMESTAMP    | NOT NULL                   |

**Indexes:**
- Index on `asset_id`
- Index on `use_case`

---

## SECTION 4: TABLE RELATIONSHIPS

### ER Diagram (Text-Based)
```
departments (1) ──< employees (N)
employees (1) ──< asset_assignments (N)
assets (1) ──< maintenance_records (N)
assets (1) ──< asset_assignments (N)
assets (1) ──< ai_insights (N)
```

### Foreign Key Explanations
- `employees.department_id` → `departments.id`
- `asset_assignments.asset_id` → `assets.id`
- `asset_assignments.employee_id` → `employees.id`
- `maintenance_records.asset_id` → `assets.id`
- `ai_insights.asset_id` → `assets.id`

### Relational Integrity
- All foreign keys enforce referential integrity via ON DELETE CASCADE/RESTRICT as appropriate.
- Unique constraints prevent duplicate records.
- Indexes optimize join and search operations.

---

## SECTION 5: BUSINESS LOGIC EXPLANATION

### ADMIN LOGIC
- **Create/Update/Delete Assets:** Validates uniqueness, persists asset, updates status, logs audit.
- **Assign Asset:** Validates asset and employee, creates assignment, updates asset status, logs audit.
- **Log Maintenance:** Validates asset, creates maintenance record, updates asset status, logs audit.
- **Upload Image/Document:** Validates file, uploads to Cloudinary, updates asset record, logs audit.
- **Generate AI Insights:** Collects asset data, builds prompt, sends to Gemini, parses response, stores insight.
- **Dashboard Analytics:** Aggregates asset, maintenance, and AI data for summary views.
- **Depreciation Calculation:** Computes straight-line depreciation, updates asset value, logs audit.
- **Report Generation:** Aggregates and exports asset, maintenance, and AI data.

#### Service Layer Process
- Input validation
- Business rule enforcement
- Transactional database updates
- Audit logging

#### Validation Rules
- Unique fields (serial number, email, department code)
- Referential integrity (FK checks)
- Status transitions (asset, assignment, maintenance)

#### Database Updates
- Insert/update/delete records
- Status field changes
- Audit trail entries

#### Status Changes
- Asset: AVAILABLE → ASSIGNED → UNDER_MAINTENANCE → RETIRED
- Assignment: ASSIGNED → RETURNED
- Maintenance: SCHEDULED → IN_PROGRESS → COMPLETED → CANCELLED

#### Audit Behavior
- All critical actions logged with user, timestamp, and action details.

### EMPLOYEE LOGIC
- **View Assigned Assets:** Fetches assignments for employee, returns asset details.
- **Request Maintenance:** Submits maintenance request, creates record, logs audit.
- **View Asset History:** Fetches asset assignments and maintenance history.
- **View AI Insights:** Returns read-only AI insights for assigned assets.

#### Service Layer Process
- Authorization checks
- Input validation
- Data retrieval
- Audit logging

#### Validation Rules
- Ownership checks (employee can only access own assets)
- Status checks (asset must be assigned)

#### Database Updates
- Maintenance request creation
- Audit trail entries

#### Status Changes
- Assignment: ASSIGNED → RETURNED
- Maintenance: SCHEDULED → IN_PROGRESS → COMPLETED

#### Audit Behavior
- All employee actions logged for compliance.

---

## SECTION 6: ENDPOINT DOCUMENTATION

### AUTH
- **POST /api/auth/login**
  - Role: ALL
  - Description: Authenticate user and issue JWT
  - Request:
    ```json
    {
      "email": "user@example.com",
      "password": "securePassword123"
    }
    ```
  - Response:
    ```json
    {
      "token": "...",
      "expiresIn": 3600,
      "role": "ADMIN",
      "user": { ... }
    }
    ```
  - Errors: 401 Unauthorized, 400 Validation

- **POST /api/auth/register**
  - Role: ADMIN
  - Description: Register new employee
  - Request:
    ```json
    {
      "fullName": "Jane Smith",
      "email": "jane.smith@example.com",
      "password": "strongPassword",
      "departmentId": "..."
    }
    ```
  - Response:
    ```json
    {
      "id": "...",
      "fullName": "Jane Smith",
      "email": "jane.smith@example.com",
      "role": "EMPLOYEE"
    }
    ```
  - Errors: 409 Conflict, 400 Validation

### ADMIN
- **POST /api/admin/assets**
  - Role: ADMIN
  - Description: Create new asset
  - Request:
    ```json
    {
      "assetName": "Dell Laptop",
      "assetType": "IT Equipment",
      "purchaseCost": 100000,
      "serialNumber": "DL-12345",
      "purchaseDate": "2026-01-01",
      "vendor": "Dell",
      "location": "HQ",
      "usefulLife": 60,
      "salvageValue": 5000
    }
    ```
  - Response:
    ```json
    {
      "id": "...",
      "assetName": "Dell Laptop",
      "status": "AVAILABLE",
      ...
    }
    ```
  - Errors: 409 Conflict, 400 Validation

- **PUT /api/admin/assets/{id}**
  - Role: ADMIN
  - Description: Update asset
  - Request: Asset fields
  - Response: Updated asset
  - Errors: 404 Not Found, 409 Conflict

- **DELETE /api/admin/assets/{id}**
  - Role: ADMIN
  - Description: Delete asset
  - Response: Success message
  - Errors: 404 Not Found

- **POST /api/admin/assets/{id}/assign**
  - Role: ADMIN
  - Description: Assign asset to employee
  - Request:
    ```json
    {
      "employeeId": "...",
      "dueBackAt": "2026-03-01",
      "notes": "Project X"
    }
    ```
  - Response:
    ```json
    {
      "assignmentId": "...",
      "status": "ASSIGNED"
    }
    ```
  - Errors: 400 Bad Request, 404 Not Found

- **POST /api/admin/assets/{id}/upload-image**
  - Role: ADMIN
  - Description: Upload asset image
  - Request: Multipart file
  - Response:
    ```json
    {
      "imageUrl": "https://res.cloudinary.com/..."
    }
    ```
  - Errors: 502 Bad Gateway, 400 Validation

- **POST /api/admin/assets/{id}/upload-document**
  - Role: ADMIN
  - Description: Upload asset document
  - Request: Multipart file
  - Response:
    ```json
    {
      "documentUrl": "https://res.cloudinary.com/..."
    }
    ```
  - Errors: 502 Bad Gateway, 400 Validation

- **POST /api/admin/assets/{id}/maintenance**
  - Role: ADMIN
  - Description: Log maintenance
  - Request:
    ```json
    {
      "type": "Repair",
      "scheduledFor": "2026-02-28",
      "vendor": "TechFix",
      "notes": "Screen replacement"
    }
    ```
  - Response:
    ```json
    {
      "maintenanceId": "...",
      "status": "SCHEDULED"
    }
    ```
  - Errors: 400 Bad Request, 404 Not Found

- **POST /api/admin/assets/{id}/ai-insight**
  - Role: ADMIN
  - Description: Generate AI insight for asset
  - Request:
    ```json
    {
      "useCase": "RiskAssessment"
    }
    ```
  - Response:
    ```json
    {
      "insightId": "...",
      "result": { ... }
    }
    ```
  - Errors: 502 Bad Gateway, 400 Validation

### EMPLOYEE
- **GET /api/employee/assets**
  - Role: EMPLOYEE
  - Description: View assigned assets
  - Response:
    ```json
    [
      {
        "assetId": "...",
        "assetName": "Dell Laptop",
        "status": "ASSIGNED",
        ...
      }
    ]
    ```
  - Errors: 403 Forbidden

- **POST /api/employee/assets/{id}/maintenance-request**
  - Role: EMPLOYEE
  - Description: Request maintenance
  - Request:
    ```json
    {
      "type": "Repair",
      "notes": "Battery issue"
    }
    ```
  - Response:
    ```json
    {
      "maintenanceId": "...",
      "status": "SCHEDULED"
    }
    ```
  - Errors: 400 Bad Request, 403 Forbidden

- **GET /api/employee/assets/{id}/history**
  - Role: EMPLOYEE
  - Description: View asset history
  - Response:
    ```json
    {
      "assignments": [ ... ],
      "maintenanceRecords": [ ... ]
    }
    ```
  - Errors: 403 Forbidden, 404 Not Found

- **GET /api/employee/assets/{id}/ai-insights**
  - Role: EMPLOYEE
  - Description: View AI insights (read-only)
  - Response:
    ```json
    [
      {
        "insightId": "...",
        "useCase": "RiskAssessment",
        "result": { ... }
      }
    ]
    ```
  - Errors: 403 Forbidden

### AI
- **POST /api/ai/generate-insight**
  - Role: ADMIN
  - Description: Generate AI insight for asset
  - Request:
    ```json
    {
      "assetId": "...",
      "useCase": "RiskAssessment"
    }
    ```
  - Response:
    ```json
    {
      "insightId": "...",
      "result": { ... }
    }
    ```
  - Errors: 502 Bad Gateway, 400 Validation

### REPORTS
- **GET /api/admin/reports/inventory**
  - Role: ADMIN
  - Description: Inventory report
  - Response:
    ```json
    [
      {
        "assetId": "...",
        "assetName": "Dell Laptop",
        "status": "AVAILABLE",
        ...
      }
    ]
    ```
  - Errors: 400 Validation

- **GET /api/admin/reports/maintenance-costs**
  - Role: ADMIN
  - Description: Maintenance cost report
  - Response:
    ```json
    [
      {
        "assetId": "...",
        "totalCost": 1200.00
      }
    ]
    ```
  - Errors: 400 Validation

- **GET /api/admin/reports/depreciation**
  - Role: ADMIN
  - Description: Depreciation report
  - Response:
    ```json
    [
      {
        "assetId": "...",
        "depreciationSchedule": [ ... ]
      }
    ]
    ```
  - Errors: 400 Validation

### MAINTENANCE
- **POST /api/admin/assets/{id}/maintenance**
  - Role: ADMIN
  - Description: Log maintenance
  - Request: Maintenance fields
  - Response: Maintenance record
  - Errors: 400 Bad Request, 404 Not Found

- **GET /api/admin/assets/{id}/maintenance-records**
  - Role: ADMIN
  - Description: View maintenance records
  - Response: List of maintenance records
  - Errors: 404 Not Found

### ASSETS
- **GET /api/admin/assets**
  - Role: ADMIN
  - Description: List all assets
  - Response: List of assets
  - Errors: 400 Validation

- **GET /api/admin/assets/{id}**
  - Role: ADMIN
  - Description: Get asset details
  - Response: Asset details
  - Errors: 404 Not Found

---

## SECTION 7: DEPRECIATION LOGIC

### Straight-Line Formula
```
Depreciation = (Purchase Cost - Salvage Value) / Useful Life
```

#### Asset Age Calculation
- Asset age = Current date - Purchase date

#### Accumulated Depreciation
- Accumulated depreciation = Depreciation × Asset age (in months)

#### Current Value Update
- Current value = Purchase cost - Accumulated depreciation

#### Recalculation
- Occurs on asset update, scheduled batch job, or report generation.

---

## SECTION 8: MAINTENANCE LOGIC

### Maintenance Record Creation
- Validates asset existence and status.
- Creates maintenance record with status SCHEDULED.
- Logs audit entry.

### Cost Aggregation
- Aggregates maintenance costs per asset for reporting and dashboard.

### High-Maintenance Detection
- Flags assets with maintenance costs above configurable threshold.

### Maintenance Due Logic
- Identifies assets with maintenance scheduled in next 30 days.

#### Sample Maintenance Record JSON
```json
{
  "maintenanceId": "...",
  "assetId": "...",
  "type": "Repair",
  "status": "SCHEDULED",
  "scheduledFor": "2026-02-28",
  "cost": 250.00,
  "vendor": "TechFix",
  "notes": "Screen replacement"
}
```

---

## SECTION 9: AI INTEGRATION LOGIC (GEMINI)

### Asset Data Collection
- Gathers asset details, maintenance history, assignment history.

### Structured Prompt Building
- Constructs prompt with asset fields, maintenance, and assignment data.

### Gemini API Request
- Sends structured JSON payload to Gemini API endpoint.

#### Sample Gemini API Request
```json
{
  "asset": {
    "id": "...",
    "name": "Dell Laptop",
    "category": "IT Equipment",
    "purchaseCost": 100000,
    "usefulLife": 60,
    "maintenanceRecords": [ ... ]
  },
  "useCase": "RiskAssessment"
}
```

### Response Parsing
- Parses Gemini API JSON response for risk, repair/replace, cost-benefit, optimization.

#### Sample Gemini API Response
```json
{
  "riskScore": 0.85,
  "recommendation": "Replace",
  "costBenefit": {
    "repairCost": 1200,
    "replacementCost": 1500,
    "benefit": "Replacement is more cost-effective"
  },
  "optimization": {
    "suggestedLocation": "Branch Office"
  }
}
```

### Insight Storage
- Stores response in `ai_insights` table with asset reference and use case.

---

## SECTION 10: CLOUDINARY INTEGRATION

### Image/Document Upload
- Validates file type and size.
- Uploads file to Cloudinary.
- Receives `secure_url` from Cloudinary.
- Updates asset record with file URL.
- Only ADMIN can upload files.

#### Validation Rules
- File type: JPEG, PNG, PDF
- Max size: 10MB

#### Example Upload Response JSON
```json
{
  "secure_url": "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg",
  "public_id": "assets/sample.jpg"
}
```

---

## SECTION 11: DASHBOARD & REPORTING LOGIC

### Aggregation Queries
- Total assets by category/status
- Maintenance cost aggregation
- AI summary aggregation

### Filtering & Pagination
- Supports query parameters for filtering (category, status, date range)
- Pagination via `page` and `size` parameters

#### Sample Dashboard JSON Response
```json
{
  "totalAssets": 120,
  "assetsByCategory": {
    "IT Equipment": 40,
    "Furniture": 30,
    "Vehicles": 50
  },
  "maintenanceCosts": {
    "IT Equipment": 5000,
    "Furniture": 1200,
    "Vehicles": 8000
  },
  "aiInsights": {
    "highRisk": 10,
    "recommendReplace": 5
  }
}
```

---

## SECTION 12: ERROR HANDLING

### Global Exception Handler
- Catches all unhandled exceptions and returns structured error response.

### Validation Error Structure
```json
{
  "error": "ValidationError",
  "message": "Field 'email' must be unique",
  "details": [ ... ]
}
```

### Unauthorized Response
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### Forbidden Response
```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

### Standard API Response Format
```json
{
  "success": false,
  "error": "...",
  "message": "...",
  "data": null
}
```

---

## SECTION 13: PERFORMANCE & SCALABILITY

### Indexing Strategy
- Unique and foreign key indexes on all critical fields.
- Composite indexes for frequent query patterns.

### Pagination Strategy
- All list endpoints support pagination via `page` and `size`.

### Caching (Optional)
- Frequently accessed reports and dashboard data can be cached.

### Async AI Calls
- AI insight generation is performed asynchronously to avoid blocking user requests.

### Database Optimization
- Use of connection pooling, query optimization, and batch operations.

### Future Microservices Transition
- Modular service design enables migration to microservices architecture.
- API gateway and service registry can be introduced for scalability.

---

**End of BACKEND.md**
