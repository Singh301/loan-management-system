package com.sudhanshu.loanmanagement.dashboard.service.impl;

import com.sudhanshu.loanmanagement.dashboard.dto.DashboardResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final LoanRepository loanRepository;

    @Override
    public DashboardResponseDto getLoanDashboard() {

        return DashboardResponseDto.builder()
                .totalLoans(loanRepository.count())
                .pendingLoans(
                        loanRepository.countByLoanStatus(
                                LoanStatus.PENDING))
                .approvedLoans(
                        loanRepository.countByLoanStatus(
                                LoanStatus.APPROVED))
                .rejectedLoans(
                        loanRepository.countByLoanStatus(
                                LoanStatus.REJECTED))
                .totalApprovedAmount(
                        loanRepository.getTotalApprovedLoanAmount())
                .build();
    }

}




