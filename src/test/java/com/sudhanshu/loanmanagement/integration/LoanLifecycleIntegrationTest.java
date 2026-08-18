package com.sudhanshu.loanmanagement.integration;

import com.sudhanshu.loanmanagement.loan.domain.LoanStateMachine;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanLifecycleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LoanStateMachine stateMachine;

    @Autowired
    private EmiCalculator emiCalculator;

    @Test
    @DisplayName("Flyway migrations applied successfully")
    void flywayMigrationsApplied() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history", Integer.class);
        assertNotNull(count);
        assertTrue(count >= 9, "Expected at least V1–V9 migrations");
    }

    @Test
    @DisplayName("State machine bean is available and enforces rules")
    void stateMachineIsWired() {
        assertDoesNotThrow(() ->
                stateMachine.validateTransition(LoanStatus.PENDING, LoanStatus.APPROVED));
        assertThrows(Exception.class, () ->
                stateMachine.validateTransition(LoanStatus.PENDING, LoanStatus.DISBURSED));
    }

    @Test
    @DisplayName("EMI calculator produces consistent results under real Spring context")
    void emiCalculatorWorksInContext() {
        BigDecimal emi = emiCalculator.calculateEmi(
                new BigDecimal("500000"), new BigDecimal("10.5"), 24);
        assertNotNull(emi);
        assertEquals(2, emi.scale());
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Core tables exist after migration")
    void coreTablesExist() {
        Integer loanCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loans", Integer.class);
        assertNotNull(loanCount);
    }
}
