<div align="center">

# 🛒 SmartOrder

### A modern, event-driven microservices ecosystem for multi-vendor retail & ordering

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![Next.js](https://img.shields.io/badge/Next.js-14.2-black.svg)](https://nextjs.org/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Driven-231F20.svg)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://docs.docker.com/compose/)

</div>

---

## 🚀 Project Overview

**SmartOrder** is a production-grade, **multi-module, event-driven microservices platform** powering a modern multi-vendor retail and ordering marketplace. It is engineered around the principles of **Domain-Driven Design**, **Hexagonal (Ports & Adapters) Architecture**, and **asynchronous, event-first communication**.

The platform lets customers browse a rich product catalog, manage shopping carts as guests or authenticated users, and place orders — while sellers manage their own storefronts through a dedicated dashboard. Every meaningful state change (a user registering, a product being approved, an account being locked) is emitted as a **domain event over Apache Kafka**, enabling loose coupling, full auditability, and independent scaling of each capability.

**Key characteristics:**

- ⚡ **High-performance & reactive at the edge** — a non-blocking Spring Cloud Gateway (WebFlux) fronts the entire system.
- 🧩 **Independently deployable services** — each bounded context owns its data store and lifecycle.
- 📡 **Event-driven backbone** — Kafka decouples producers (auth, product) from consumers (notifications, audit logging).
- 🛡️ **Resilient by design** — Resilience4j circuit breakers, rate limiting, and retries guard every route.
- 🔍 **Fully observable** — distributed tracing (Zipkin/Brave), Prometheus metrics, and correlation-ID propagation across every hop.
- 🖥️ **Modern SSR frontend** — a Next.js 14 App Router application with a standalone Docker output.

---

## 🏗️ Architecture & Component Breakdown

SmartOrder is split into **infrastructure services** (the platform backbone), **core business services** (the bounded contexts), and a **Next.js frontend**, all wired together over a shared Docker network and discovered dynamically through Eureka.

```
                         ┌──────────────────────────┐
                         │      Next.js Frontend     │  :3000
                         │  (App Router · Tailwind)  │
                         └────────────┬─────────────┘
                                      │ HTTPS / JSON
                         ┌────────────▼─────────────┐
                         │       API Gateway         │  :8080
                         │ JWT auth · Resilience4j   │
                         │ Rate-limit · CORS · CB    │
                         └─────┬───────────────┬─────┘
            X-Auth-* headers   │               │   lb:// (Eureka)
              ┌────────────────┘               └────────────────┐
              ▼                ▼                ▼                ▼
       ┌────────────┐  ┌────────────┐   ┌────────────┐  ┌──────────────────┐
       │   Auth     │  │  Product   │   │   Cart     │  │  Notification    │
       │  :8081     │  │  :8082     │   │  :8085     │  │  :8086           │
       │ PG · Redis │  │ PG · ES ·  │   │  Redis     │  │ Mongo · Kafka    │
       │ · Kafka    │  │ MinIO·Kafka│   │            │  │ (consumer)       │
       └─────┬──────┘  └─────┬──────┘   └────────────┘  └────────▲─────────┘
             │  publish      │  publish                          │  consume
             └───────────────┴──────────► Apache Kafka ──────────┘
                                          (event backbone)

        Backbone:  Eureka (:8761)  ·  Config Server (:8888)  ·  Zipkin (:9411)
```

### 🧱 Infrastructure Services

| Service | Port | Responsibility |
|---|---|---|
| **Discovery Server** (Netflix Eureka) | `8761` | Service registry & discovery. Every service registers itself and resolves peers via `lb://` URIs. Secured with basic auth. |
| **Config Server** (Spring Cloud Config) | `8888` | Centralized, environment-aware configuration served from a **native** `config-repo/`. Provides global defaults and per-service overrides. Secured with basic auth. |
| **API Gateway** (Spring Cloud Gateway / WebFlux) | `8080` | The single, reactive entry point. Validates JWTs, forwards identity via `X-Auth-*` headers, and applies **Resilience4j Circuit Breakers**, **Redis-backed rate limiting**, **retries**, and **global CORS**. |

### 🧠 Core Business Services

| Service | Port | Data Stores | Description |
|---|---|---|---|
| **Auth Service** | `8081` | PostgreSQL · Redis · Kafka | User management, registration, login, JWT issuance, refresh-token rotation (Redis), and account lockout. **Publishes** auth domain events (`user-registered`, `account-locked`, `password-changed`, …). |
| **Product Service** | `8082` | PostgreSQL · Elasticsearch · MinIO · Kafka | Product catalog, full-text search (Elasticsearch), image storage (MinIO/S3), seller review workflow (submit → approve/reject). **Publishes** product lifecycle events. |
| **Cart Service** | `8085` | Redis | Blazing-fast shopping cart for both **guests** (via `X-Guest-Cart-Id`) and authenticated users, with guest-to-user cart merging on login. |
| **Notification Service** ✨ | `8086` | MongoDB · Kafka | **Newly implemented, fully event-driven.** Consumes auth & product events from Kafka, persists an immutable **audit log** + **notification log** to MongoDB, and dispatches transactional emails (welcome, account-locked, password-changed) via SMTP. |

> **Placeholder contexts** — `order-service`, `inventory-service`, and `seller-service` are reserved modules in the build, scaffolded for upcoming bounded contexts and already wired into the gateway's routing table.

### 🎨 Frontend

A **Next.js 14 (App Router)** application styled with **Tailwind CSS**, delivering server-side-rendered, SEO-friendly storefront pages and rich client-side interactivity.

- **App Router** with server components for product listing/detail SSR and client components for cart, auth, and the seller dashboard.
- **State management** via Zustand (auth & cart stores) with `@tanstack/react-query` for data fetching.
- **Edge Middleware** for route protection and role-based redirects (`/seller`, `/admin`).
- **Standalone Docker output** (`output: 'standalone'`) for a minimal, self-contained production image.
- **Axios interceptors** for silent JWT refresh and correlation-ID propagation.

---

## 🛠️ Tech Stack

<table>
<tr>
<td valign="top" width="50%">

### ⚙️ Backend
- **Java 21** (Gradle toolchain)
- **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1**
  - Cloud Gateway (WebFlux)
  - Netflix Eureka (Discovery)
  - Config Server
- **Resilience4j** (Circuit Breaker / Retry)
- **Spring Security** + **JJWT** (JWT)
- **Spring Data JPA / Redis / MongoDB**
- **Lombok**
- **Hexagonal Architecture** (Ports & Adapters)

</td>
<td valign="top" width="50%">

### 🖥️ Frontend
- **Next.js 14.2** (App Router)
- **React 18**
- **TypeScript 5**
- **Tailwind CSS 3**
- **Zustand** (state)
- **TanStack React Query**
- **React Hook Form** + **Zod**
- **Axios** · **Lucide Icons** · **React Hot Toast**

</td>
</tr>
<tr>
<td valign="top">

### 🗄️ Databases & Message Brokers
- **PostgreSQL 16** (Auth · Product · Order)
- **MongoDB 7** (Notification audit/logs)
- **Redis 7** (Cart · refresh tokens · rate limiting)
- **Apache Kafka 7.6** + **Zookeeper** (event bus)
- **Elasticsearch 8.13** (product search)
- **MinIO** (S3-compatible object storage)

</td>
<td valign="top">

### 🚢 DevOps & Infrastructure
- **Docker** & **Docker Compose**
- **Gradle 8.8** (multi-module build)
- **Eureka** (service discovery)
- **Spring Cloud Config** (centralized config)
- **Zipkin** + **Micrometer Tracing (Brave)**
- **Prometheus** (metrics export)
- **Kafka UI** (topic inspection)

</td>
</tr>
</table>

---

## 🚦 How to Run & Deploy

### ✅ Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| **Docker** & **Docker Compose** | Latest | For the full orchestrated stack |
| **JDK** | **21** | A Gradle toolchain is configured (Spring Boot 3.2.5 + Gradle 8.8) |
| **Node.js** | **18+** (20/24 recommended) | For the Next.js frontend |

> ℹ️ The backend builds against **Java 21** via the Gradle toolchain — no system-wide JDK switch is required if Gradle can locate a 21 toolchain.

### 🐳 Quick Start — Full Stack (Recommended)

Spin up **everything** (databases, Kafka, all services, and the frontend) with a single command:

```bash
docker-compose up --build -d
```

Then verify and inspect:

```bash
docker-compose ps                     # check all containers are healthy
docker-compose logs -f api-gateway    # tail a specific service
docker-compose down                   # stop the stack (keeps volumes)
docker-compose down -v                # stop and wipe all data volumes
```

| Surface | URL |
|---|---|
| 🖥️ Frontend | http://localhost:3000 |
| 🚪 API Gateway | http://localhost:8080 |
| 🧭 Eureka Dashboard | http://localhost:8761 |
| 📬 Kafka UI | http://localhost:8090 |
| 🔎 Zipkin Tracing | http://localhost:9411 |
| 🗃️ MinIO Console | http://localhost:9001 |

### 🔧 Local Development Build

**Backend (Gradle):**

```bash
# Compile & package every module (executable Spring Boot jars + plain libs)
./gradlew build              # on Windows: gradlew.bat build

# Compile without running tests
./gradlew build -x test

# Run an individual service
./gradlew :services:api-gateway:bootRun
```

> Real services (`api-gateway`, `auth-service`, `product-service`, `cart-service`, `notification-service`, `config-server`, `eureka-server`) produce executable Spring Boot jars; empty placeholder modules produce plain library jars.

**Frontend (npm):**

```bash
cd frontend
npm install        # install dependencies
npm run dev        # start the dev server → http://localhost:3000
npm run build      # production build (type-check + standalone output)
npm run start      # serve the production build
```

### 🥾 Manual Boot Order (without Docker)

When running services directly on the host, start infrastructure first, then services **in dependency order**:

```bash
docker-compose up -d postgres-auth redis kafka mongodb   # backing stores
./gradlew :services:eureka-server:bootRun     # 1. registry      → :8761
./gradlew :services:config-server:bootRun     # 2. config server → :8888
./gradlew :services:auth-service:bootRun      # 3. auth          → :8081
./gradlew :services:api-gateway:bootRun       # 4. gateway       → :8080
# ...then product / cart / notification as needed
```

### 🔑 Default Development Credentials

| Purpose | Username | Password | Override env var |
|---|---|---|---|
| Config Server (basic auth) | `configuser` | `configpass` | `CONFIG_SERVER_USER` / `CONFIG_SERVER_PASSWORD` |
| Eureka (basic auth) | `eurekauser` | `eurekapass` | `EUREKA_USER` / `EUREKA_PASSWORD` |
| JWT signing secret | — | `MyUltraSecure…ChangeInProd` | `JWT_SECRET` |
| PostgreSQL / Redis | `smartorder` | `smartorder` | — |

> ⚠️ These are **development defaults only**. Always override them via environment variables in any real deployment.

---

## 📂 Project Structure

```
smartorder/
├── build.gradle                  # Root build — shared deps, Lombok, BOMs, toolchain
├── settings.gradle               # Multi-module composition
├── docker-compose.yml            # Full-stack orchestration (infra + services + UI)
├── gradlew / gradlew.bat         # Gradle wrapper (8.8)
│
├── shared/
│   └── common-lib/               # Cross-cutting library
│       └── src/main/java/com/smartorder/common/
│           ├── audit/            # Shared audit metadata
│           ├── exception/        # ErrorCode, ErrorResponse, SmartOrderException
│           └── filter/           # CorrelationIdFilter
│
├── services/
│   ├── eureka-server/            # 🧭 Service Discovery        (:8761)
│   ├── config-server/            # ⚙️  Centralized Config       (:8888)
│   │   └── src/main/resources/config-repo/   # native config backend
│   ├── api-gateway/              # 🚪 Edge Gateway             (:8080)
│   │   └── .../gateway/
│   │       ├── security/         # JwtAuthenticationFilter, JwtProperties
│   │       ├── ratelimit/        # Redis token-bucket config
│   │       ├── filter/           # CorrelationIdGatewayFilter
│   │       └── fallback/         # Circuit-breaker fallback controllers
│   │
│   ├── auth-service/             # 🔐 Auth & JWT               (:8081)
│   │   └── .../auth/{domain,ports,adapters}   # Hexagonal layers
│   ├── product-service/          # 📦 Catalog & Search         (:8082)
│   ├── cart-service/             # 🛒 Shopping Cart            (:8085)
│   ├── notification-service/     # 📨 Events → Mongo + Email   (:8086)
│   │   └── .../notification/
│   │       ├── adapters/messaging/   # Kafka consumers + email
│   │       ├── adapters/config/      # KafkaConsumerConfig
│   │       └── domain/model/         # AuditLog, NotificationLog (Mongo docs)
│   │
│   ├── order-service/            # 🧾 (reserved bounded context)
│   ├── inventory-service/        # 📊 (reserved bounded context)
│   └── seller-service/           # 🏪 (reserved bounded context)
│
└── frontend/                     # 🎨 Next.js 14 App Router
    ├── next.config.mjs           # standalone output + image/rewrite config
    ├── Dockerfile                # multi-stage standalone build
    └── src/
        ├── app/                  # App Router routes (auth, products, seller)
        ├── components/           # UI: layout, product, cart, seller
        ├── lib/                  # axios client + API modules
        ├── store/                # Zustand stores (auth, cart)
        ├── hooks/                # useAuth and friends
        ├── middleware.ts         # route protection & role guards
        └── types/                # shared TypeScript models
```

---

## 🔒 Security & Gateway Header Logic

SmartOrder uses a **centralized, edge-authentication** model. The **API Gateway is the single point of JWT validation**, and downstream microservices never re-parse tokens — they simply **trust signed identity headers** forwarded by the gateway. This keeps each service lightweight and stateless while ensuring a single, auditable security boundary.

### 1️⃣ Authentication at the Edge

A global `JwtAuthenticationFilter` (`order = -100`, runs before all routing) intercepts every request:

```
Client ──► API Gateway ──► JwtAuthenticationFilter
                              │
                              ├─ Is path PUBLIC?  (login, register, refresh,
                              │     GET products/search, /actuator)  ──► pass through
                              │
                              ├─ Missing/!"Bearer "  ──► 401 TOKEN_INVALID
                              │
                              └─ Validate HMAC-SHA signature (JJWT)
                                    ├─ expired      ──► 401 "Token has expired"
                                    ├─ malformed    ──► 401 "Token is invalid"
                                    └─ valid ──► inject X-Auth-* headers, forward
```

- **Public paths** (login, register, refresh, public product reads, actuator) bypass authentication entirely.
- The token signature is verified against the shared `JWT_SECRET` (HMAC-SHA) — the **same secret** configured on `auth-service`, so the gateway can validate any token the auth service issues.
- Invalid, missing, or expired tokens are rejected at the edge with a structured JSON error (`401 / TOKEN_INVALID`) — they never reach a business service.

### 2️⃣ Safe Claim Forwarding via `X-Auth-*` Headers

On successful validation, the gateway **mutates the request**, decoding the JWT claims into trusted headers before proxying it onward:

| Header | Source claim | Purpose |
|---|---|---|
| `X-Auth-User-Id` | `sub` (subject) | The authenticated user's ID |
| `X-Auth-Username` | `username` | The username |
| `X-Auth-Role` | `role` | Role for downstream authorization (`CUSTOMER`, `SELLER`, `ADMIN`, …) |
| `X-Correlation-Id` | incoming or generated | End-to-end request tracing across services |

### 3️⃣ Trust at the Service Layer

Each business service registers a lightweight `GatewayHeaderAuthFilter` that reads these headers and **rehydrates the Spring Security context** — *without* re-parsing the JWT:

```java
String userId = request.getHeader("X-Auth-User-Id");
String role   = request.getHeader("X-Auth-Role");
// → builds a UsernamePasswordAuthenticationToken with ROLE_<role>
//   and populates SecurityContextHolder.
```

This lets services use standard Spring Security primitives (`@PreAuthorize`, `authenticated()`, role checks) while the heavy lifting stays at the gateway. The Cart Service additionally supports anonymous identity via an `X-Guest-Cart-Id` header, enabling seamless guest carts that merge into the user's cart on login.

> 🛡️ **Defense in depth:** Because the `X-Auth-*` headers are only trustworthy when traffic enters through the gateway, services must not be exposed directly to untrusted networks. In production, place all services on a private network where the gateway is the **only** publicly reachable ingress.

---

<div align="center">

**SmartOrder** — engineered for scale, resilience, and developer joy. ⚡

</div>
