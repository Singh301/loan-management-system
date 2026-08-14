package com.sudhanshu.loanmanagement.loan.entity;

public enum LoanStatus {

    PENDING,        // Application submitted
    APPROVED,       // Approved by manager/admin
    REJECTED,       // Rejected
    DISBURSED,      // Money disbursed to customer
    ACTIVE,         // EMI cycle running
    OVERDUE,        // Missed EMI
    NPA,            // Non-Performing Asset (90+ days overdue)
    CLOSED,         // Fully paid / foreclosed
    WRITTEN_OFF     // Written off as bad debt
}