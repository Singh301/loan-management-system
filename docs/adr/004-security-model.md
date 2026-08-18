# ADR-004: Security Model

## Status
Accepted

## Context
API is exposed over HTTP and handles financial data. Needs JWT, logout safety, brute-force protection, and upload safety.

## Decision
- Stateless JWT access tokens + DB-backed refresh tokens
- Access-token blacklist on logout (in-memory; Redis for multi-instance)
- Rate limit on `/api/v1/auth/**` (20 req/min/IP)
- Account lockout after 5 failed logins (15 minutes)
- File uploads: content-type + extension allowlist, 5MB max, UUID filenames, path-traversal checks
- Method security via `@PreAuthorize`

## Consequences
- Strong baseline for interviews and production hardening path
