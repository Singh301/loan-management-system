# ADR-002: Explicit Loan State Machine

## Status
Accepted

## Context
Loan status changes were guarded by scattered if-checks. Illegal transitions (e.g. REJECTED → DISBURSED) were possible under concurrent or mistaken API calls.

## Decision
Centralize allowed transitions in `LoanStateMachine`. All status-changing services must call `validateTransition(current, target)` before mutating status.

## Allowed transitions
- PENDING → APPROVED | REJECTED
- APPROVED → DISBURSED | REJECTED
- DISBURSED → ACTIVE
- ACTIVE → OVERDUE | CLOSED | WRITTEN_OFF
- OVERDUE → ACTIVE | NPA | CLOSED | WRITTEN_OFF
- NPA → CLOSED | WRITTEN_OFF
- REJECTED / CLOSED / WRITTEN_OFF → (terminal)

## Consequences
- Domain rules are explicit and testable
- Concurrent illegal transitions fail fast with 409 Conflict
