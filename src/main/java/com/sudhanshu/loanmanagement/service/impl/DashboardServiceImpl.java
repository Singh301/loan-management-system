package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.DashboardResponseDto;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.DashboardService;
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