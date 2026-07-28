package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;


import java.util.List;

public interface LoanService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

    List<LoanResponseDto> getAllLoans();

    LoanResponseDto getLoanById(Long loanId);

    List<LoanResponseDto> getLoansByCustomer(Long customerId);

    LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto);

    LoanResponseDto updateLoanStatus(Long loanId, LoanStatusUpdateDto requestDto);
}