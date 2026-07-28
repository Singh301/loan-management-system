package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import java.util.List;

public interface LoanService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

    List<LoanResponseDto> getAllLoans();

    LoanResponseDto getLoanById(Long loanId);

    List<LoanResponseDto> getLoansByCustomer(Long customerId);

}