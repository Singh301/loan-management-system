package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;

public interface LoanService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

}