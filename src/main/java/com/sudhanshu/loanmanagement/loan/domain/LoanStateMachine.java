package com.sudhanshu.loanmanagement.loan.domain;

import com.sudhanshu.loanmanagement.exception.InvalidLoanStateException;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Central place that defines and enforces allowed loan status transitions.
 * Prevents illegal state changes such as REJECTED → DISBURSED.
 */
@Component
public class LoanStateMachine {

    private final Map<LoanStatus, Set<LoanStatus>> allowedTransitions;

    public LoanStateMachine() {
        allowedTransitions = new EnumMap<>(LoanStatus.class);

        // Application submitted
        allowedTransitions.put(LoanStatus.PENDING, EnumSet.of(
                LoanStatus.APPROVED,
                LoanStatus.REJECTED
        ));

        // Approved by manager/admin – ready for disbursement
        allowedTransitions.put(LoanStatus.APPROVED, EnumSet.of(
                LoanStatus.DISBURSED,
                LoanStatus.REJECTED          // rare: cancelled before disbursement
        ));

        // Money given to customer – EMI cycle starts
        allowedTransitions.put(LoanStatus.DISBURSED, EnumSet.of(
                LoanStatus.ACTIVE
        ));

        // EMI cycle running
        allowedTransitions.put(LoanStatus.ACTIVE, EnumSet.of(
                LoanStatus.OVERDUE,
                LoanStatus.CLOSED,           // fully paid / foreclosed
                LoanStatus.WRITTEN_OFF
        ));

        // Missed EMI
        allowedTransitions.put(LoanStatus.OVERDUE, EnumSet.of(
                LoanStatus.ACTIVE,           // customer caught up
                LoanStatus.NPA,
                LoanStatus.CLOSED,
                LoanStatus.WRITTEN_OFF
        ));

        // Non-performing asset (90+ days)
        allowedTransitions.put(LoanStatus.NPA, EnumSet.of(
                LoanStatus.CLOSED,           // recovered
                LoanStatus.WRITTEN_OFF
        ));

        // Terminal states – no further transitions
        allowedTransitions.put(LoanStatus.REJECTED, EnumSet.noneOf(LoanStatus.class));
        allowedTransitions.put(LoanStatus.CLOSED, EnumSet.noneOf(LoanStatus.class));
        allowedTransitions.put(LoanStatus.WRITTEN_OFF, EnumSet.noneOf(LoanStatus.class));
    }

    /**
     * Validates whether a transition from current → target is allowed.
     * Throws InvalidLoanStateException if not allowed.
     */
    public void validateTransition(LoanStatus current, LoanStatus target) {
        if (current == null || target == null) {
            throw new InvalidLoanStateException("Current or target status cannot be null");
        }

        if (current == target) {
            throw new InvalidLoanStateException(
                    "Loan is already in status: " + current);
        }

        Set<LoanStatus> allowed = allowedTransitions.getOrDefault(
                current, EnumSet.noneOf(LoanStatus.class));

        if (!allowed.contains(target)) {
            throw new InvalidLoanStateException(current.name(), target.name());
        }
    }

    /**
     * Returns true if the transition is legal (does not throw).
     */
    public boolean canTransition(LoanStatus current, LoanStatus target) {
        if (current == null || target == null || current == target) {
            return false;
        }
        Set<LoanStatus> allowed = allowedTransitions.getOrDefault(
                current, EnumSet.noneOf(LoanStatus.class));
        return allowed.contains(target);
    }

    /**
     * Returns the set of statuses that can be reached from the given status.
     */
    public Set<LoanStatus> getAllowedNextStatuses(LoanStatus current) {
        return EnumSet.copyOf(
                allowedTransitions.getOrDefault(current, EnumSet.noneOf(LoanStatus.class)));
    }
}
