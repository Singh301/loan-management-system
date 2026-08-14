package com.sudhanshu.loanmanagement.exception;

/**
 * Thrown when an illegal loan status transition is attempted.
 */
public class InvalidLoanStateException extends DomainException {

    public InvalidLoanStateException(String message) {
        super(message);
    }

    public InvalidLoanStateException(String currentStatus, String targetStatus) {
        super(String.format(
                "Invalid loan status transition from %s to %s",
                currentStatus, targetStatus));
    }
}
