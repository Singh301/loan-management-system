package com.sudhanshu.loanmanagement.repayment.service;

import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentRequestDto;
import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentResponseDto;

import java.util.List;

public interface LoanRepaymentService {

    LoanRepaymentResponseDto repayLoan(
            LoanRepaymentRequestDto dto);

    List<LoanRepaymentResponseDto> getRepaymentHistory(
            Long loanId);

}




