package com.sudhanshu.loanmanagement.exception;

/**
 * Base class for domain / business rule violations.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
