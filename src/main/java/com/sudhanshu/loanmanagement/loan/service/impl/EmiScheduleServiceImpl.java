package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.domain.LoanStateMachine;
import com.sudhanshu.loanmanagement.loan.dto.EmiScheduleResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.mapper.LoanMapper;
import com.sudhanshu.loanmanagement.loan.repository.EmiScheduleRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.loan.service.EmiScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiScheduleServiceImpl implements EmiScheduleService {

    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanStateMachine loanStateMachine;
    private final LoanMapper loanMapper;

    @Override
    @Transactional
    public List<EmiScheduleResponseDto> generateAndPersistEmiSchedule(Long loanId) {
        Loan loan = getActiveLoan(loanId);

        if (loan.getLoanStatus() != LoanStatus.APPROVED
                && loan.getLoanStatus() != LoanStatus.DISBURSED
                && loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "EMI Schedule can only be generated for approved / disbursed / active loans.");
        }

        if (loan.getEmi() == null) {
            throw new IllegalStateException("EMI is not calculated yet. Approve the loan first.");
        }

        // If already exists, return existing
        List<EmiSchedule> existing = emiScheduleRepository
                .findByLoanLoanIdOrderByInstallmentNumberAsc(loanId);
        if (!existing.isEmpty()) {
            return existing.stream().map(loanMapper::toEmiDto).toList();
        }

        LocalDate startDate = loan.getDisbursementDate() != null
                ? loan.getDisbursementDate()
                : LocalDate.now();

        List<EmiSchedule> schedules = buildScheduleEntities(loan, startDate);
        emiScheduleRepository.saveAll(schedules);

        return schedules.stream().map(loanMapper::toEmiDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponseDto> getEmiSchedule(Long loanId) {
        getActiveLoan(loanId); // existence + soft-delete check
        return emiScheduleRepository
                .findByLoanLoanIdOrderByInstallmentNumberAsc(loanId)
                .stream()
                .map(loanMapper::toEmiDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmiScheduleResponseDto> previewEmiSchedule(Long loanId) {
        Loan loan = getActiveLoan(loanId);

        if (loan.getLoanStatus() != LoanStatus.APPROVED
                && loan.getLoanStatus() != LoanStatus.DISBURSED
                && loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "EMI Schedule can only be generated for approved loans.");
        }

        if (loan.getEmi() == null) {
            throw new IllegalStateException("EMI is not calculated yet.");
        }

        LocalDate startDate = loan.getDisbursementDate() != null
                ? loan.getDisbursementDate()
                : LocalDate.now();

        return buildScheduleEntities(loan, startDate).stream()
                .map(loanMapper::toEmiDto)
                .toList();
    }

    @Override
    @Transactional
    public void markOverdueLoans() {
        LocalDate today = LocalDate.now();
        log.info("Starting Overdue + NPA marking job. Date: {}", today);

        List<EmiSchedule> overdueEmis = emiScheduleRepository.findOverdueEmis(today);

        int overdueCount = 0;
        int npaCount = 0;

        for (EmiSchedule emi : overdueEmis) {
            if (emi.getStatus() != EmiStatus.OVERDUE) {
                emi.setStatus(EmiStatus.OVERDUE);

                if (emi.getLateFee() == null || emi.getLateFee().compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal lateFee = BigDecimal.valueOf(500);
                    emi.setLateFee(lateFee);

                    Loan loan = emi.getLoan();
                    loan.setTotalLateFee(
                            (loan.getTotalLateFee() != null ? loan.getTotalLateFee() : BigDecimal.ZERO)
                                    .add(lateFee)
                    );
                }

                emiScheduleRepository.save(emi);
                overdueCount++;
            }

            Loan loan = emi.getLoan();
            if (Boolean.TRUE.equals(loan.getDeleted())) {
                continue;
            }

            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(emi.getDueDate(), today);

            // ACTIVE → OVERDUE
            if (loan.getLoanStatus() == LoanStatus.ACTIVE
                    && loanStateMachine.canTransition(LoanStatus.ACTIVE, LoanStatus.OVERDUE)) {
                loan.setLoanStatus(LoanStatus.OVERDUE);
                loanRepository.save(loan);
            }

            // → NPA after 90 days
            if (daysOverdue > 90
                    && loan.getLoanStatus() != LoanStatus.NPA
                    && loan.getLoanStatus() != LoanStatus.CLOSED
                    && loan.getLoanStatus() != LoanStatus.WRITTEN_OFF
                    && loanStateMachine.canTransition(loan.getLoanStatus(), LoanStatus.NPA)) {

                loan.setLoanStatus(LoanStatus.NPA);
                loanRepository.save(loan);
                npaCount++;
                log.warn("Loan marked as NPA. loanId={}, daysOverdue={}", loan.getLoanId(), daysOverdue);
            }
        }

        log.info("Overdue + NPA job completed. EMIs marked overdue: {}, Loans marked NPA: {}",
                overdueCount, npaCount);
    }

    // -------------------- private helpers --------------------

    private Loan getActiveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id : " + loanId));

        if (Boolean.TRUE.equals(loan.getDeleted())) {
            throw new ResourceNotFoundException("Loan not found with id : " + loanId);
        }
        return loan;
    }

    private List<EmiSchedule> buildScheduleEntities(Loan loan, LocalDate startDate) {
        List<EmiSchedule> schedules = new ArrayList<>();

        BigDecimal balance = loan.getLoanAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal emi = loan.getEmi();
        int tenure = loan.getTenureMonths();

        for (int month = 1; month <= tenure; month++) {
            BigDecimal interest = balance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principal = emi.subtract(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            if (principal.compareTo(balance) > 0) {
                principal = balance;
            }

            balance = balance.subtract(principal).setScale(2, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            schedules.add(EmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(month)
                    .dueDate(startDate.plusMonths(month))
                    .emiAmount(emi)
                    .principalComponent(principal)
                    .interestComponent(interest)
                    .outstandingPrincipalAfter(balance)
                    .status(EmiStatus.PENDING)
                    .build());
        }

        return schedules;
    }
}
