package com.sudhanshu.loanmanagement.loan.repository;

import java.math.BigDecimal;

public interface LoanAnalyticsProjection {

    Long getTotalLoans();

    BigDecimal getTotalAmount();

    BigDecimal getAverageLoanAmount();
}




