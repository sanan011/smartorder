# SmartOrder

A multi-module Spring Boot 3.2.5 microservices project (Java 21, Gradle, Spring Cloud 2023.0.x).

## Modules

| Module | Port | Description |
|---|---|---|
| `services/eureka-server` | 8761 | Service registry (Netflix Eureka) |
| `services/config-server` | 8888 | Centralized config (native backend, `config-repo/`) |
| `services/api-gateway` | 8080 | Edge gateway (Spring Cloud Gateway, WebFlux) |
| `services/auth-service` | 8081 | Authentication / JWT issuing, Postgres + Redis + Kafka |
| `services/{product,order,cart,inventory,seller,notification}-service` | — | Placeholder modules (no application code yet) |
| `shared/common-lib` | — | Shared library (JPA-annotated audit types, DTOs) |

## Prerequisites

- **JDK 21** (a Gradle toolchain is configured)
- **Docker** (for local infrastructure)
- The bundled Gradle wrapper (`./gradlew`) — Gradle **8.8** (required; Spring Boot 3.2.5 is not compatible with Gradle 9)

## Build

```bash
./gradlew clean build -x test
```

Real services (`api-gateway`, `auth-service`, `config-server`, `eureka-server`) produce executable Spring Boot jars; the empty placeholder modules produce plain jars.

## Local infrastructure (Docker)

`docker-compose.yml` provides the backing services with the credentials/ports the apps expect.

```bash
docker compose up -d      # start Postgres, Redis, Kafka
docker compose ps         # all three should report (healthy)
docker compose down       # stop (keeps data)
docker compose down -v    # stop and drop volumes (fresh DB)
```

| Service | Host port | Credentials / notes |
|---|---|---|
| PostgreSQL 16 | 5432 | db `smartorder_auth`, user `smartorder`, password `smartorder` |
| Redis 7 | 6379 | no password |
| Kafka 3.7 (KRaft) | 9092 | single-node, advertised on `localhost:9092` |

> **Port 5432 conflict:** if you have a native PostgreSQL installed, its service may occupy 5432 and shadow the Docker container. Stop it (Windows, elevated terminal):
> ```powershell
> net stop postgresql-x64-18
> sc config postgresql-x64-18 start= demand   # don't auto-start on reboot
> ```

## Running the services

Start infrastructure first (`docker compose up -d`), then launch the services **in order**, each in its own terminal:

```bash
./gradlew :services:eureka-server:bootRun     # 1. registry      → http://localhost:8761
./gradlew :services:config-server:bootRun      # 2. config server → http://localhost:8888
./gradlew :services:auth-service:bootRun       # 3. auth          → http://localhost:8081
./gradlew :services:api-gateway:bootRun        # 4. gateway       → http://localhost:8080
```

On first boot, `auth-service` runs its Flyway migrations (`V1__create_users_table`, `V2__create_audit_log_table`) against Postgres automatically.

### Default credentials

| Purpose | Username | Password | Override env var |
|---|---|---|---|
| Config Server basic auth | `configuser` | `configpass` | `CONFIG_SERVER_USER` / `CONFIG_SERVER_PASSWORD` |
| Eureka dashboard / registration | `eurekauser` | `eurekapass` | `EUREKA_USER` / `EUREKA_PASSWORD` |
| JWT signing secret | — | `MyUltraSecureJwtSecretKeyForDevOnly!ChangeInProd` | `JWT_SECRET` |

These are **development defaults** — override them via environment variables in any real deployment.

## Notes

- `config-server` uses the **native** backend (`spring.profiles.active: native`), serving `services/config-server/src/main/resources/config-repo/`. Restart it after changing those files.
- `auth-service` carries local fallbacks for its datasource and JWT settings, so it boots even when the Config Server isn't serving; config-repo values take precedence when it is.
- If ports appear stuck after stopping a service, a `bootRun` JVM may have been orphaned — clear strays with `taskkill //F //IM java.exe` (Windows) or `pkill -f bootRun` (Unix).
