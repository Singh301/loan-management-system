package com.sudhanshu.loanmanagement.loan.event;

import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;

/**
 * Published when a loan changes status (approved, rejected, disbursed, etc.).
 */
public record LoanStatusChangedEvent(
        Long loanId,
        Long customerUserId,
        LoanStatus previousStatus,
        LoanStatus newStatus,
        String remarks,
        String performedBy
) {}
