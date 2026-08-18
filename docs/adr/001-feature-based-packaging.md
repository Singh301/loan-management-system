# ADR-001: Feature-Based Packaging (Modular Monolith)

## Status
Accepted

## Context
Layered architecture (controller/service/repository packages) becomes hard to navigate as features grow. Loan, customer, repayment, and document domains have clear boundaries.

## Decision
Organize code by **feature** (auth, customer, loan, repayment, document, audit, notification, dashboard) with internal layers inside each feature.

## Consequences
- High cohesion within features
- Clear ownership for interviews and team work
- Easier extraction into microservices later if needed
- Some shared code still lives in `config`, `security`, `exception`, `dto`
