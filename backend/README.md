# Prerequisites
- JDK 21+
- Maven 3.9+

# Local run

Run commands from the backend directory (`MiASI/backend`), because JSON database
paths are configured relative to that module.

```powershell
cd backend
mvn "-Djava.version=21" "-Dspotless.check.skip=true" spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

# Focused API tests

```powershell
cd backend
mvn "-Djava.version=21" "-Dspotless.check.skip=true" "-Dtest=ConfControllerIT,ScheduleControllerIT" test
```

# Full verification

`mvn clean verify` currently runs Spotless for the whole codebase. On the current
branch it can fail on pre-existing formatting violations in files unrelated to
the catalog API changes. Run it only after formatting the whole backend or after
updating the Spotless configuration.
