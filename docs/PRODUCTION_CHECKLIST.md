# Production Readiness Checklist

## Implemented in codebase
- [x] Loan state machine + optimistic locking + idempotent disbursement
- [x] Customer data isolation (`OwnershipGuard`)
- [x] Token blacklist (Redis when available, in-memory fallback)
- [x] Rate limiting + login lockout
- [x] Hardened uploads + S3/local storage abstraction
- [x] Domain events + transactional outbox
- [x] Custom metrics + correlation ID (MDC)
- [x] Production DB indexes (V10)
- [x] HikariCP pool tuning in prod profile
- [x] CI security scan (Trivy)
- [x] No JWT/DB defaults in `application-prod.yml` (env only)
- [x] Actuator health public; details hidden in prod

## Ops (must configure in AWS)
- [ ] RDS private subnet + automated backups (7–35 days)
- [ ] ALB HTTPS only (ACM certificate)
- [ ] SSM/Secrets Manager for all secrets
- [ ] ECS task role least privilege + S3 bucket policy
- [ ] Redis (ElastiCache) for multi-instance blacklist/cache
- [ ] `FILE_STORAGE=s3` + `S3_BUCKET` + IRSA/instance role
- [ ] CloudWatch alarms: 5xx, CPU, unhealthy targets, Flyway failures
- [ ] Enable Redis by removing Redis auto-config excludes when `REDIS_HOST` is set

## Still recommended (not fully automated here)
- [ ] Automated integration tests (Testcontainers) on money flows
- [ ] Virus scan hook on document upload
- [ ] Blue/green or canary deploy
- [ ] Formal dependency license review
