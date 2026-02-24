# Project Workflow

## Overview
- Backend: Spring Boot 3.5 (Java 21), PostgreSQL, JWT auth.
- Frontend: React 19 + Vite, axios client with bearer token, admin UI pages (dashboard, departments, employees, assets, assignments, maintenance, reports).
- Ports: backend 8091, frontend 5173 (default Vite).

## Prerequisites
- Java 21 (match `pom.xml`).
- Maven 3.9+.
- Node.js 20+ and npm.
- PostgreSQL running locally (default url `jdbc:postgresql://localhost:5432/inventory`).

## Configuration
- Backend config lives in `src/main/resources/application.properties`.
  - Update DB URL/username/password to your local values.
  - Replace `security.jwt.secret` with a strong secret; adjust `security.jwt.expiration-seconds` if needed.
  - Set `app.cors.allowed-origins` to the frontend origin (e.g., http://localhost:5173).
  - Cloudinary and Gemini keys are placeholders; replace with valid credentials or remove those features.
- Frontend uses `frontend/src/services/apiClient.js` base URL pointing to the backend; adjust if the backend host/port changes.

## Install Dependencies
```bash
# Backend (from repo root)
mvn -q -DskipTests compile

# Frontend (from frontend/)
npm install
```

## Run in Development
```bash
# Terminal 1: backend (repo root)
mvn spring-boot:run

# Terminal 2: frontend (frontend/)
npm run dev -- --host
```
- Open http://localhost:5173.
- Login to obtain a JWT; the UI stores it and sends it as Bearer for API calls.

## Build for Production
```bash
# Backend jar (repo root)
mvn clean package
# Artifact: target/InventoryManagement-0.0.1-SNAPSHOT.jar

# Frontend static build (frontend/)
npm run build
# Output: frontend/dist
```
- Serve `frontend/dist` behind a web server and point it to the running backend API.

## Testing and Checks
```bash
# Backend tests
mvn test

# Frontend lint
npm run lint
```

## Common Tasks
- Seed data: create users/departments/assets via admin APIs or UI after login.
- Update CORS/ports: edit `application.properties` to match your deployment topology.
- Rotate secrets: change JWT, DB, Cloudinary, Gemini keys before deploying.

## Troubleshooting
- Connection errors: verify PostgreSQL is running and credentials match `application.properties`.
- 401/403 responses: ensure you logged in and the JWT is present in local storage; restart the frontend if the token expired.
- CORS blocks: confirm the frontend origin is included in `app.cors.allowed-origins`.
