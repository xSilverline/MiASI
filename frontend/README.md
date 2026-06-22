# Frontend

React/Vite client for Mars Mission Planner.

## Requirements

- Node.js 20+.
- Backend running on `http://localhost:8080` for live API calls.

## Install

```powershell
npm install
```

## Run

```powershell
npm run dev
```

Open `http://127.0.0.1:5173/`.

The development server proxies `/api` to `http://localhost:8080`. For another backend URL, set:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

## Build and checks

```powershell
npm run build
npm run lint
```

`npm run build` runs `tsc -b && vite build`.

## Implemented screens

- Session: login, verify and logout through `/api/auth`.
- Mission: default plan, plan by id, plan count and resource types through `/api/conf`.
- Catalog: module catalog and module states through `/api/conf`.
- Schedule: create/load schedule, fetch timeline, generate/load/approve scenario draft and add
  `THREAT`, `SUPPLY_DELIVERY` or `MODULE_STATE_CHANGE` events through `/api/schedule`.

API routes and DTO types are centralized in `src/api.ts`; UI code should not introduce scattered
endpoint strings.
