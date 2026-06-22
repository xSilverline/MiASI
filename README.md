# Mars Mission Planner

Mars Mission Planner is a Spring Boot + React application for planning a Mars mission, managing
mission configuration, scheduling operational events, generating scenarios and running analysis.

## Repository layout

- `backend` - Spring Boot backend, hexagonal application services, domain model, REST API and tests.
- `frontend` - React/Vite mission operations client.
- `scripts/verify-project.ps1` - full local verification for backend and frontend.

## Requirements

- JDK 21.
- Node.js 20+ with npm.
- Maven 3.9+ is optional. If Maven is missing, `backend/scripts/verify-backend.ps1` downloads a local
  Maven distribution into `backend/.mvn`.

## Verify everything

From `MiASI`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-project.ps1
```

This runs:

```powershell
.\backend\scripts\verify-backend.ps1
npm run build
```

Backend verification executes `clean verify`, ArchUnit tests, integration tests and Jacoco. Frontend
verification executes `tsc -b && vite build`.

## Run locally

Start the backend from `MiASI/backend`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-backend.ps1 -MavenArgs spring-boot:run
```

Start the frontend from `MiASI/frontend`:

```powershell
npm install
npm run dev
```

Open `http://127.0.0.1:5173/`. In development, Vite proxies `/api` to
`http://localhost:8080`, so the frontend can call the backend through relative API URLs.

For a non-default backend URL, set:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

## API surface used by the frontend

- Auth: `POST /api/auth/login`, `POST /api/auth/{token}/verify`,
  `POST /api/auth/{token}/logout`.
- Configuration: `GET /api/conf/default/plan`, `GET /api/conf/{id}/plan`,
  `GET /api/conf/plans-count`, `GET /api/conf/module-catalog`.
- Schedule: `POST /api/schedule`, `GET /api/schedule/{id}`,
  `GET /api/schedule/{id}/timeline`, `POST /api/schedule/{id}/events`,
  `POST /api/schedule/scenario`, `POST /api/schedule/scenario/{draftId}/approve`.

## Seed data and persistence

The backend reads JSON seed/test data through configuration properties in
`backend/src/main/resources/application.properties` and test properties under
`backend/src/test/resources`. Persistence tests that exercise JSON storage use temporary directories
where possible; integration tests restore test database files after mutation.

## Useful commands

```powershell
# Backend only
cd backend
powershell -ExecutionPolicy Bypass -File .\scripts\verify-backend.ps1

# Frontend only
cd frontend
npm run build
npm run lint

# Production frontend preview after build
npm run preview
```
