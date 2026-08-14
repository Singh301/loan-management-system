# Loan Management System

Enterprise-style **Spring Boot 3** REST API for loan lifecycle management: application → multi-level approval → disbursement → EMI → repayment → foreclosure.

Built as a **feature-based modular monolith** with production-oriented patterns suitable for 4+ years Java backend interviews.

---

## Tech Stack

| Area | Choice |
|------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.4 |
| Security | Spring Security + JWT (JJWT 0.12) + Refresh Token |
| Persistence | Spring Data JPA + MySQL 8 + Flyway |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Observability | Actuator + Prometheus + Micrometer custom metrics |
| Caching | Caffeine (in-process) |
| Resilience | Resilience4j (circuit breaker / retry foundation) |
| Mapping | MapStruct |
| Packaging | Docker multi-stage + Docker Compose |

---

## Core Features

- JWT authentication + refresh token + **logout blacklist**
- Role-based access: `ADMIN`, `MANAGER`, `CUSTOMER`
- Loan application, **multi-level approval** (Manager → Admin), rejection
- **Explicit loan state machine** with illegal-transition protection
- Disbursement with **idempotency key**
- EMI schedule generation & overdue / NPA marking (scheduler)
- Repayments, foreclosure, collateral, documents
- Soft delete (Hibernate `@SQLRestriction`)
- Optimistic locking (`@Version`) on critical entities
- Domain events + **transactional outbox**
- Audit logging & in-app notifications
- Dashboard / analytics APIs
- Rate limiting on auth endpoints
- Account lockout after failed logins
- Hardened file upload validation

---

## Architecture Overview

```
HTTP Request
    │
    ├─ RateLimitingFilter          (auth endpoints only)
    ├─ RequestLoggingFilter        (MDC correlationId)
    ├─ JwtAuthenticationFilter     (blacklist check)
    │
    ▼
Controllers  (/api/v1/...)
    │
    ▼
Focused Services
    ├─ LoanApplicationService
    ├─ LoanApprovalService      (+ LoanStateMachine)
    ├─ LoanDisbursementService  (+ idempotency)
    ├─ EmiScheduleService
    └─ ...
    │
    ├─ publish Domain Events  ──► LoanEventListener (AFTER_COMMIT)
    │                                  ├─ Notification
    │                                  ├─ Audit
    │                                  └─ Metrics
    │
    └─ enqueue OutboxEvents   ──► OutboxProcessor (scheduled)
                                       └─ future Kafka / webhooks
```

### Package layout (feature-based)

```
com.sudhanshu.loanmanagement
├── auth / user / customer / loan / repayment / document
├── audit / notification / dashboard
├── security          # JWT, blacklist, lockout, rate limit
├── outbox            # Transactional outbox
├── metrics           # Custom business metrics
├── config            # Cache, OpenAPI, Health, Resilience
├── exception         # DomainException hierarchy
└── filter / aop
```

---

## Loan Status Lifecycle

```
PENDING ──► APPROVED ──► DISBURSED ──► ACTIVE ──► OVERDUE ──► NPA
   │            │                         │          │
   └── REJECTED │                         ├── CLOSED │
                └── REJECTED              └── WRITTEN_OFF
```

Terminal states: `REJECTED`, `CLOSED`, `WRITTEN_OFF`.

Multi-level approval:
1. **Manager** records Level-1 approval (loan stays `PENDING`)
2. **Admin** records Level-2 approval → transition to `APPROVED` + EMI calculation

---

## Key Design Decisions (ADRs)

See [`docs/adr/`](docs/adr/):

| ADR | Topic |
|-----|--------|
| [001](docs/adr/001-feature-based-packaging.md) | Feature-based packaging |
| [002](docs/adr/002-loan-state-machine.md) | Explicit state machine |
| [003](docs/adr/003-transactional-outbox.md) | Transactional outbox |
| [004](docs/adr/004-security-model.md) | Security model |
| [005](docs/adr/005-caching-strategy.md) | Caching strategy |

---

## API Base Path

All APIs are versioned under:

```
/api/v1
```

| Module | Base |
|--------|------|
| Auth | `/api/v1/auth` |
| Users | `/api/v1/users` |
| Customers | `/api/v1/customers` |
| Loans | `/api/v1/loans` |
| Loan Products | `/api/v1/loan-products` |
| Repayments | `/api/v1/repayments` |
| Documents | `/api/v1/documents` |
| Notifications | `/api/v1/notifications` |
| Dashboard | `/api/v1/dashboard` |
| Audits | `/api/v1/audits` |

Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/api-docs`

---

## Security Highlights

| Control | Detail |
|---------|--------|
| Auth | JWT access + DB refresh token |
| Logout | Refresh deleted + access token blacklisted |
| Rate limit | 20 req/min/IP on `/api/v1/auth/**` |
| Lockout | 5 failed logins → 15 min lock |
| Uploads | PDF/JPEG/PNG/WEBP, 5MB, UUID names, path-safe |
| Method security | `@PreAuthorize` on controllers |

---

## Observability

- **Correlation ID**: send or receive `X-Request-Id`
- **MDC fields**: `correlationId`, `username`, `method`, `api`, `status`, `executionTime`, `ip`
- **Custom metrics** (Prometheus):
  - `loan.applied` / `loan.approved` / `loan.rejected` / `loan.disbursed` / `loan.overdue`
  - `loan.approval.duration`
- **Health**: `/actuator/health` (includes upload-dir indicator)
- **Prometheus**: `/actuator/prometheus`

---

## Running Locally

### Prerequisites
- JDK 21
- Maven 3.9+
- MySQL 8 (or use Docker Compose)

### Option A – Docker Compose

```bash
cp .env.example .env   # set passwords / JWT secret
docker compose up --build
```

App: `http://localhost:8080`  
MySQL: `localhost:3307`

### Option B – Local

```bash
# Start MySQL and create DB: loan_management_system
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

Flyway runs migrations automatically (`V1` … `V9`).

---

## Configuration Notes

| Key | Purpose |
|-----|---------|
| `jwt.secret` | HS256 key (min 256-bit) – **must** override in prod |
| `jwt.expiration` | Access token TTL (ms) |
| `jwt.refresh-expiration` | Refresh token TTL (ms) |
| `file.upload-dir` | Document storage path |
| `outbox.poll-interval-ms` | Outbox processor interval (default 10000) |

Prod profile uses env vars only (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, …).  
`ddl-auto: validate` – schema owned by Flyway.

---

## Scalability Notes (Interview Talking Points)

1. **Single instance today** – Caffeine cache + in-memory token blacklist  
2. **Horizontal scale** – move blacklist & cache to Redis; sticky sessions not required (stateless JWT)  
3. **Write path** – optimistic locking + state machine prevent double disbursement / illegal transitions  
4. **Side effects** – outbox enables reliable publish to Kafka without dual-write risk  
5. **Read path** – cache products / dashboard; later CQRS read models if analytics grows  
6. **External calls** – Resilience4j circuit breaker + retry ready for payment/KYC integrations  

---

## Sequence: Approve → Disburse → EMI

```
Customer                Manager/Admin              System
   │                         │                       │
   │  POST /loans (apply)    │                       │
   │────────────────────────────────────────────────►│ PENDING
   │                         │                       │
   │                         │  PUT status APPROVED  │
   │                         │  (Manager level 1)    │
   │                         │──────────────────────►│ approval row
   │                         │  PUT status APPROVED  │
   │                         │  (Admin level 2)      │
   │                         │──────────────────────►│ APPROVED + EMI
   │                         │                       │ event + outbox
   │                         │  POST /disburse       │
   │                         │  (+ Idempotency-Key)  │
   │                         │──────────────────────►│ DISBURSED→ACTIVE
   │                         │                       │ EMI schedule rows
   │  GET /emi-schedule      │                       │
   │────────────────────────────────────────────────►│
```

---

## Project Evolution (Phased Improvements)

| Phase | Focus |
|-------|--------|
| 1 | State machine, optimistic lock, service split, multi-level approval, soft delete, idempotency |
| 2 | MapStruct, domain events, exception hierarchy, JJWT 0.12, API versioning |
| 3 | Token blacklist, rate limit, lockout, hardened uploads |
| 4 | MDC correlation, business metrics, Caffeine cache, upload health |
| 5 | Transactional outbox, Resilience4j, ADRs, architecture docs |

---

## License

Personal / portfolio project.
