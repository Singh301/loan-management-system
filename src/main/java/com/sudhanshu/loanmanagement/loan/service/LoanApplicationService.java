package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;

/**
 * Handles loan application creation and updates while the loan is still PENDING.
 */
public interface LoanApplicationService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

    LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto);
}
