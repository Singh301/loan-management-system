package com.sudhanshu.loanmanagement.loan.event;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Published after a loan is successfully disbursed.
 */
public record LoanDisbursedEvent(
        Long loanId,
        Long customerUserId,
        BigDecimal amount,
        LocalDate disbursementDate
) {}
