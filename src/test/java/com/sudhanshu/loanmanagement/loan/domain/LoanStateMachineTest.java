package com.sudhanshu.loanmanagement.loan.domain;

import com.sudhanshu.loanmanagement.exception.InvalidLoanStateException;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoanStateMachineTest {

    private LoanStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new LoanStateMachine();
    }

    @Test
    @DisplayName("PENDING can go to APPROVED or REJECTED")
    void pendingAllowedTransitions() {
        Set<LoanStatus> allowed = stateMachine.getAllowedNextStatuses(LoanStatus.PENDING);
        assertTrue(allowed.contains(LoanStatus.APPROVED));
        assertTrue(allowed.contains(LoanStatus.REJECTED));
        assertEquals(2, allowed.size());
    }

    @Test
    @DisplayName("Terminal states have no further transitions")
    void terminalStatesHaveNoTransitions() {
        assertTrue(stateMachine.getAllowedNextStatuses(LoanStatus.REJECTED).isEmpty());
        assertTrue(stateMachine.getAllowedNextStatuses(LoanStatus.CLOSED).isEmpty());
        assertTrue(stateMachine.getAllowedNextStatuses(LoanStatus.WRITTEN_OFF).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, APPROVED", "PENDING, REJECTED", "APPROVED, DISBURSED",
            "DISBURSED, ACTIVE", "ACTIVE, OVERDUE", "ACTIVE, CLOSED",
            "OVERDUE, NPA", "OVERDUE, ACTIVE", "NPA, CLOSED"
    })
    @DisplayName("Valid transitions are accepted")
    void validTransitions(String from, String to) {
        assertDoesNotThrow(() -> stateMachine.validateTransition(
                LoanStatus.valueOf(from), LoanStatus.valueOf(to)));
        assertTrue(stateMachine.canTransition(LoanStatus.valueOf(from), LoanStatus.valueOf(to)));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDING, DISBURSED", "PENDING, ACTIVE", "APPROVED, ACTIVE",
            "DISBURSED, CLOSED", "ACTIVE, PENDING", "REJECTED, APPROVED",
            "CLOSED, ACTIVE", "WRITTEN_OFF, CLOSED", "NPA, ACTIVE"
    })
    @DisplayName("Illegal transitions throw InvalidLoanStateException")
    void illegalTransitions(String from, String to) {
        assertThrows(InvalidLoanStateException.class,
                () -> stateMachine.validateTransition(LoanStatus.valueOf(from), LoanStatus.valueOf(to)));
        assertFalse(stateMachine.canTransition(LoanStatus.valueOf(from), LoanStatus.valueOf(to)));
    }

    @Test
    @DisplayName("Same status transition is rejected")
    void sameStatusRejected() {
        assertThrows(InvalidLoanStateException.class,
                () -> stateMachine.validateTransition(LoanStatus.ACTIVE, LoanStatus.ACTIVE));
    }

    @Test
    @DisplayName("Null status throws")
    void nullStatusThrows() {
        assertThrows(InvalidLoanStateException.class,
                () -> stateMachine.validateTransition(null, LoanStatus.APPROVED));
    }

    @ParameterizedTest
    @EnumSource(LoanStatus.class)
    @DisplayName("getAllowedNextStatuses never returns null")
    void allowedNextNeverNull(LoanStatus status) {
        assertNotNull(stateMachine.getAllowedNextStatuses(status));
    }
}
