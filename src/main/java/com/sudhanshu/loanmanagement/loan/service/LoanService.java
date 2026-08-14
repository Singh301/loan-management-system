package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.PageResponse;
import com.sudhanshu.loanmanagement.loan.dto.*;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import org.springframework.data.domain.Page;


import java.math.BigDecimal;
import java.util.List;

public interface LoanService {

    LoanResponseDto applyLoan(LoanRequestDto requestDto);

    List<LoanResponseDto> getLoansByType(LoanType loanType);

    List<LoanResponseDto> getAllLoans(LoanType loanType);

    LoanResponseDto getLoanById(Long loanId);

    List<LoanResponseDto> getLoansByCustomer(Long customerId);

    List<LoanResponseDto> getMyLoans(String username);

    LoanResponseDto getMyLoan(Long loanId, String username);

    LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto);

    LoanResponseDto updateLoanStatus(Long loanId, LoanStatusUpdateDto requestDto);

    Page<LoanResponseDto> getLoansWithPagination(int page, int size, String sortBy, String direction);

    Page<LoanResponseDto> getLoansByStatus(LoanStatus loanStatus, int page, int size);

    Page<LoanResponseDto> getLoansByType(LoanType loanType, int page, int size);

    Page<LoanResponseDto> searchLoans(
            String customerName,
            String email,
            LoanType loanType,
            LoanStatus loanStatus,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int page,
            int size,
            String sortBy,
            String direction
    );

    CustomerLoanHistoryResponseDto getCustomerLoanHistory(Long customerId);

    LoanStatisticsResponseDto getLoanStatistics();

    List<LoanMonthlyReportDto> getMonthlyLoanReport();

    LoanAnalyticsResponseDto getLoanAnalytics(
            LoanType loanType);

    List<EmiScheduleResponseDto> generateEmiSchedule(Long loanId);

    LoanForeclosureResponseDto getForeclosureDetails(
            Long loanId,
            Integer paidInstallments);

    LoanStatementResponseDto getLoanStatement(Long loanId);

    Page<LoanResponseDto> getAllLoansPaginated(
            LoanType loanType,
            int page,
            int size,
            String sortBy,
            String direction
    );

    // Add these method signatures

    LoanResponseDto disburseLoan(Long loanId, DisburseLoanRequestDto request);

    List<EmiScheduleResponseDto> getEmiScheduleFromDb(Long loanId);

    void markOverdueLoans();   // will be called by scheduler





}




