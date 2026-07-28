package com.sudhanshu.loanmanagement.exception;

public class LoanAlreadyProcessedException extends RuntimeException {

    public LoanAlreadyProcessedException(String message) {
        super(message);
    }
}