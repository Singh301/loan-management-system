# ADR-003: Transactional Outbox for Reliable Events

## Status
Accepted

## Context
Side effects (notifications, audit, future webhooks) must not break the main business transaction, but must not be lost if the process crashes after commit.

## Decision
1. Publish Spring domain events for in-process listeners (`@TransactionalEventListener(AFTER_COMMIT)`).
2. Also write the same payload into `outbox_events` in the **same DB transaction**.
3. `OutboxProcessor` polls PENDING rows and marks them PROCESSED (stub for Kafka/webhook).

## Consequences
- At-least-once delivery foundation
- No dual-write problem between DB and message broker
- Ready to plug in Kafka / RabbitMQ later without changing business services
