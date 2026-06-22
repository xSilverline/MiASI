# Backend

Spring Boot API for Mars Mission Planner.

## Prerequisites

- JDK 21
- Maven 3.9+ or PowerShell with network access for `scripts/verify-backend.ps1`

The project is pinned to Java 21 in `pom.xml`. On Windows, the verify script will prefer
`JAVA_HOME` when it points to JDK 21+, then it will try common Eclipse Adoptium JDK 21 paths.

## Verify

Run from `MiASI/backend`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-backend.ps1
```

The script uses global `mvn` when available. If Maven is not in `PATH`, it downloads Apache Maven
3.9.10 into `MiASI/backend/.mvn/` and runs `clean verify`.

If Maven is already installed and Java 21 is active, this direct command is also valid:

```bash
mvn clean verify
```

After verification, open `target/site/jacoco/index.html` to inspect coverage.

## Run

Run from `MiASI/backend`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\verify-backend.ps1 -MavenArgs spring-boot:run
```

With a local Maven installation and Java 21 active:

```bash
mvn spring-boot:run
```

The backend listens on `http://localhost:8080` by default. Controllers currently allow localhost
frontend origins and the Vite frontend proxies `/api` to this port in development.

## Main API groups

- `/api/auth` - login, session verification and logout.
- `/api/conf` - mission plans, resource types, module states and module catalog.
- `/api/schedule` - schedule creation, timeline, manual events, scenario drafts and approvals.

## Data and tests

Runtime and test JSON storage paths are configured through `application.properties`. Tests that use
JSON persistence should avoid permanent mutation of `src/test/resources`; current integration tests
restore the test database hardcopy after changes, and lower-level persistence tests use temporary
directories where possible.
