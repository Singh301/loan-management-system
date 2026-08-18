package com.sudhanshu.loanmanagement.loan.event;

import com.sudhanshu.loanmanagement.loan.entity.LoanType;

import java.math.BigDecimal;

/**
 * Published when a new loan application is created.
 */
public record LoanCreatedEvent(
        Long loanId,
        Long customerId,
        LoanType loanType,
        BigDecimal amount
) {}
