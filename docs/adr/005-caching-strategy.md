# ADR-005: Caching Strategy

## Status
Accepted

## Context
Loan products and dashboard aggregates are read-heavy and change infrequently.

## Decision
- Caffeine in-process cache (10 min TTL, max 500 entries)
- Cache names: `loanProducts`, `userDetails`, `dashboardStats`
- Evict on product create/update

## Consequences
- Zero extra infrastructure for local/dev and single-instance deploys
- For multi-instance production, replace `CacheManager` with Redis
