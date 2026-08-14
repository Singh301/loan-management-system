package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.DisburseLoanRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;

/**
 * Handles loan disbursement with idempotency and state-machine guards.
 */
public interface LoanDisbursementService {

    /**
     * Disburse an APPROVED loan.
     *
     * @param loanId           loan to disburse
     * @param request          disbursement details
     * @param idempotencyKey   client-supplied key to prevent double disbursement (optional but recommended)
     */
    LoanResponseDto disburseLoan(Long loanId,
                                 DisburseLoanRequestDto request,
                                 String idempotencyKey);
}
