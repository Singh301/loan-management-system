package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import org.springframework.data.domain.Page;


import java.util.List;

public interface LoanService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

    List<LoanResponseDto> getAllLoans();

    LoanResponseDto getLoanById(Long loanId);

    List<LoanResponseDto> getLoansByCustomer(Long customerId);

    LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto);

    LoanResponseDto updateLoanStatus(Long loanId, LoanStatusUpdateDto requestDto);

    Page<LoanResponseDto> getLoansWithPagination(int page, int size, String sortBy, String direction);

    Page<LoanResponseDto> getLoansByStatus(LoanStatus loanStatus, int page, int size);

    Page<LoanResponseDto> getLoansByType(LoanType loanType, int page, int size);
}