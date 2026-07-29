package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.DashboardResponseDto;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getLoanDashboard_ShouldReturnDashboardStatistics() {

        when(loanRepository.count()).thenReturn(10L);

        when(loanRepository.countByLoanStatus(LoanStatus.PENDING))
                .thenReturn(3L);

        when(loanRepository.countByLoanStatus(LoanStatus.APPROVED))
                .thenReturn(5L);

        when(loanRepository.countByLoanStatus(LoanStatus.REJECTED))
                .thenReturn(2L);

        when(loanRepository.getTotalApprovedLoanAmount())
                .thenReturn(new BigDecimal("4500000"));

        DashboardResponseDto response =
                dashboardService.getLoanDashboard();

        assertNotNull(response);

        assertEquals(10L, response.getTotalLoans());

        assertEquals(3L, response.getPendingLoans());

        assertEquals(5L, response.getApprovedLoans());

        assertEquals(2L, response.getRejectedLoans());

        assertEquals(
                new BigDecimal("4500000"),
                response.getTotalApprovedAmount());

        verify(loanRepository).count();

        verify(loanRepository)
                .countByLoanStatus(LoanStatus.PENDING);

        verify(loanRepository)
                .countByLoanStatus(LoanStatus.APPROVED);

        verify(loanRepository)
                .countByLoanStatus(LoanStatus.REJECTED);

        verify(loanRepository)
                .getTotalApprovedLoanAmount();
    }

    @Test
    void getLoanDashboard_ShouldReturnZeroStatistics() {

        when(loanRepository.count()).thenReturn(0L);

        when(loanRepository.countByLoanStatus(any()))
                .thenReturn(0L);

        when(loanRepository.getTotalApprovedLoanAmount())
                .thenReturn(BigDecimal.ZERO);

        DashboardResponseDto response =
                dashboardService.getLoanDashboard();

        assertEquals(0L, response.getTotalLoans());

        assertEquals(0L, response.getPendingLoans());

        assertEquals(0L, response.getApprovedLoans());

        assertEquals(0L, response.getRejectedLoans());

        assertEquals(BigDecimal.ZERO,
                response.getTotalApprovedAmount());
    }

    @Test
    void getLoanDashboard_ShouldReturnOnlyApprovedLoans() {

        when(loanRepository.count()).thenReturn(5L);

        when(loanRepository.countByLoanStatus(LoanStatus.PENDING))
                .thenReturn(0L);

        when(loanRepository.countByLoanStatus(LoanStatus.APPROVED))
                .thenReturn(5L);

        when(loanRepository.countByLoanStatus(LoanStatus.REJECTED))
                .thenReturn(0L);

        when(loanRepository.getTotalApprovedLoanAmount())
                .thenReturn(new BigDecimal("3000000"));

        DashboardResponseDto response =
                dashboardService.getLoanDashboard();

        assertEquals(5L, response.getApprovedLoans());

        assertEquals(
                new BigDecimal("3000000"),
                response.getTotalApprovedAmount());
    }
}