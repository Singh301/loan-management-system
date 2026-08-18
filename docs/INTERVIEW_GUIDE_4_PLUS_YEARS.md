# Loan Management System — 4+ Years Java Backend Interview Guide

This document explains the project in simple language first, then gives the senior-level explanation you can use in an interview.

## 1. Architecture: Modular Monolith

**Simple:** One deployable Spring Boot application is divided into business modules such as auth, customer, loan, repayment, audit and notification.

**Why:** It is easier to develop and operate than many microservices, while still keeping boundaries clean. Microservices should be extracted only when independent scaling, ownership or release cadence justifies the cost.

**Interview answer:** "I selected a modular monolith because the domain is cohesive and the team/system size does not justify distributed transactions and operational overhead. I use events/outbox at module boundaries so selected modules can be extracted later."

## 2. Transaction Boundary

`@Transactional` means a business operation either commits completely or rolls back completely. Loan disbursement updates the loan, creates the EMI schedule and writes an outbox event in one database transaction.

**Key concept:** ACID — Atomicity, Consistency, Isolation, Durability.

## 3. Optimistic vs Pessimistic Locking

The Loan entity has `@Version` for optimistic locking. This detects conflicting ordinary updates.

Critical money-changing flows additionally load a loan with `PESSIMISTIC_WRITE`. This serializes transactions for the same loan so two simultaneous disbursement/payment operations cannot both calculate from the same old balance.

**Interview answer:** "I use optimistic locking for normal low-contention writes and a database row lock for critical balance allocation where a lost update would create financial inconsistency."

## 4. Idempotency

A retry must not perform the same financial action twice. Disbursement accepts an idempotency key. If a client retries after a timeout, the service returns the already-created result rather than disbursing again.

For a larger payment platform, evolve this into a generic `idempotency_requests` table with `(operation, key)` unique, request hash and cached response.

## 5. EMI Rounding

Money uses `BigDecimal`, never `double`. Monthly principal/interest values are rounded to currency scale. The final installment absorbs the remaining rounding difference so outstanding principal becomes exactly zero.

## 6. Transactional Outbox

Problem: saving a loan and publishing a message are two different systems. If DB commit succeeds but message publishing fails, downstream systems miss the event.

Solution: write the business row and `outbox_events` row in the same DB transaction. A publisher later sends pending events. This gives reliable at-least-once event delivery when combined with an idempotent consumer.

## 7. At-least-once Delivery and Idempotent Consumer

Message brokers can deliver the same event more than once. Consumers therefore store an event/message ID or use a business unique key and safely ignore duplicates.

Never claim "exactly once everywhere". End-to-end financial systems normally achieve effectively-once business behavior through idempotency.

## 8. Redis and Horizontal Scaling

In-memory state belongs to one JVM only. With three ECS instances, logout state stored on instance A is invisible to B and C. Production therefore uses Redis as shared state for token revocation/rate limiting/cache.

This branch fails closed for token revocation if production Redis is unavailable. Development may use local fallback behavior.

## 9. JWT Security

Access tokens should be short-lived. Refresh tokens are longer-lived and should be rotated/revoked. Never log Authorization headers or raw tokens. Token blacklist keys use a SHA-256 fingerprint instead of the raw JWT.

## 10. Flyway

Every schema change is versioned in `db/migration`. Production uses `ddl-auto=validate`; Hibernate verifies mapping compatibility but does not silently modify the database.

**Zero-downtime rule:** prefer expand → migrate → contract. Add a new column first, deploy compatible code, migrate data, then remove the old column in a later release.

## 11. Database Indexing

Indexes speed reads but add storage/write cost. Add them for real query patterns: customer+status, due-date+status, loan+installment, outbox status+created time. Validate with query plans rather than adding indexes blindly.

## 12. Security Layers

- BCrypt hashes passwords.
- Spring Security performs authentication and RBAC.
- Method security protects business operations.
- Ownership checks prevent one customer reading another customer's data.
- Rate limiting reduces abuse.
- Production secrets come from environment/secret manager, never source code.
- Swagger is disabled in production.
- HSTS and secure headers harden HTTP responses.

## 13. Observability

Three pillars:

1. **Logs** — structured/correlated request information, no secrets/PII.
2. **Metrics** — latency, error rate, throughput, DB pool, business counters.
3. **Traces** — follow one request/event across components when OpenTelemetry is enabled.

Important production alerts include high 5xx rate, p95 latency, DB pool exhaustion, outbox backlog, Redis failure and unhealthy ECS tasks.

## 14. Health Probes

**Liveness:** is the process alive? Restart it when this fails.

**Readiness:** should this instance receive traffic? Remove it from the load balancer when it cannot safely serve requests.

Spring Boot Actuator exposes both probe groups.

## 15. CI/CD

A production pipeline should run compile → tests → integration tests → coverage/report → security scan → package/container build. Do not use `-DskipTests` as the normal CI path.

## 16. Testcontainers

H2 is not MySQL. SQL dialects, locking, indexes and migrations differ. Testcontainers starts a real disposable MySQL container for integration tests, so Flyway and repository behavior are tested against the same database family as production.

## 17. Real-time Meaning

"Real-time" here means important business events are propagated asynchronously soon after the transaction, not that every endpoint must use WebSockets. LoanApproved, LoanDisbursed, PaymentReceived and EmiOverdue are good domain events.

## 18. Resilience4j

For external KYC/credit/payment services use:

- timeout — never wait forever;
- retry — only transient and safe/idempotent failures;
- circuit breaker — stop hammering an unhealthy dependency;
- bulkhead — prevent one slow dependency consuming all request threads.

## 19. Production Deployment

Recommended AWS shape: ALB (HTTPS) → ECS service → RDS MySQL + ElastiCache Redis + private S3. Secrets Manager/SSM stores credentials. RDS/Redis should be private. S3 documents should not be public.

## 20. Common Interview Questions

**Why not microservices?** Because distributed systems add network failure, eventual consistency, deployment and observability costs. Start modular; extract only when justified.

**Why BigDecimal?** Binary floating point cannot exactly represent many decimal monetary values.

**Why both optimistic and pessimistic locking?** They solve different contention/correctness needs. Optimistic is cheap for normal writes; pessimistic serializes critical balance updates.

**What if the client times out after disbursement?** It retries with the same idempotency key and receives the existing result.

**What if message publishing fails after DB commit?** The outbox row remains pending and can be retried.

**What if a Kafka message is duplicated?** Consumer idempotency prevents duplicate business effects.

**What if Redis goes down?** Production token revocation fails closed and readiness/alerts should surface dependency failure; local development can degrade gracefully.

**How do you prevent N+1?** LAZY relations by default, targeted fetch joins/entity graphs/projections for specific read models.

**How do you deploy schema changes without downtime?** Backward-compatible Flyway migrations using expand/migrate/contract.

## 21. 90-second Project Explanation

"This is a Java 21 and Spring Boot loan-management backend built as a modular monolith. It covers customer onboarding, authentication, loan application, maker-checker approval, disbursement, EMI scheduling, repayment, delinquency, documents, audit and notifications. My main focus was financial correctness and production behavior. I use BigDecimal for money, Flyway for controlled schema changes, optimistic locking for normal concurrent updates and pessimistic locking around critical money flows. Disbursement is idempotent, and business events use a transactional outbox so database state and asynchronous processing cannot silently diverge. Security uses stateless JWT, BCrypt, RBAC, ownership controls, rate limiting and Redis-backed revocation for multi-instance deployment. The production profile disables Swagger and requires external secrets and Redis. Actuator and Prometheus expose health and metrics. CI runs tests and security scanning, and integration testing is designed around real MySQL with Testcontainers. The architecture intentionally stays modular rather than creating unnecessary microservices, but domain events provide a clean path to Kafka and service extraction when scale or team boundaries require it."
