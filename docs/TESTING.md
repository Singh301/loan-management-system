# Testing Strategy

## Goals
- Unit tests for pure domain logic (state machine, EMI calculation)
- Integration tests against real MySQL via Testcontainers
- Coverage gate in CI

## Running Tests

```bash
./mvnw verify                  # all tests
./mvnw test -Dtest='!*IntegrationTest'   # unit only
./mvnw verify -Dtest='*IntegrationTest'  # integration only (Docker required)
```

## Covered
| Area | Type | Class |
|------|------|-------|
| Loan status transitions | Unit | LoanStateMachineTest |
| EMI calculation + schedule | Unit | EmiCalculatorTest |
| Flyway + context + wiring | Integration | LoanLifecycleIntegrationTest |

## Next recommended tests
1. Full happy-path: apply → approve → disburse (idempotent) → repay
2. Concurrent disbursement attempts
3. OwnershipGuard / security tests
