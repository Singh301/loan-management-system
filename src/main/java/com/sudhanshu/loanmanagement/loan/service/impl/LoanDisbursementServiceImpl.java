package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.domain.LoanStateMachine;
import com.sudhanshu.loanmanagement.loan.dto.DisburseLoanRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.event.LoanDisbursedEvent;
import com.sudhanshu.loanmanagement.loan.mapper.LoanMapper;
import com.sudhanshu.loanmanagement.loan.repository.EmiScheduleRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.loan.service.LoanDisbursementService;
import com.sudhanshu.loanmanagement.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
public class LoanDisbursementServiceImpl implements LoanDisbursementService {

    private final LoanRepository loanRepository;
    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanStateMachine loanStateMachine;
    private final LoanMapper loanMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public LoanResponseDto disburseLoan(Long loanId, DisburseLoanRequestDto request, String idempotencyKey) {
        log.info("Disbursement requested. loanId={}", loanId);

        // SELECT ... FOR UPDATE equivalent: only one transaction can change this loan at a time.
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id : " + loanId));

        if (Boolean.TRUE.equals(loan.getDeleted())) {
            throw new ResourceNotFoundException("Loan not found with id : " + loanId);
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()
                && idempotencyKey.equals(loan.getDisbursementIdempotencyKey())
                && (loan.getLoanStatus() == LoanStatus.DISBURSED || loan.getLoanStatus() == LoanStatus.ACTIVE)) {
            log.info("Idempotent disbursement retry. loanId={}", loanId);
            return loanMapper.toResponseDto(loan);
        }

        loanStateMachine.validateTransition(loan.getLoanStatus(), LoanStatus.DISBURSED);
        if (loan.getEmi() == null || loan.getEmi().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("EMI must be calculated before disbursement. Approve the loan first.");
        }

        LocalDate disbursementDate = request.getDisbursementDate() != null
                ? request.getDisbursementDate() : LocalDate.now();

        loan.setDisbursementDate(disbursementDate);
        loan.setNextDueDate(disbursementDate.plusMonths(1));
        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setOutstandingPrincipal(loan.getLoanAmount());
        loan.setPaidInstallments(0);
        loan.setRemainingInstallments(loan.getTenureMonths());

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            loan.setDisbursementIdempotencyKey(idempotencyKey);
        }
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            loan.setRemarks(request.getRemarks());
        }

        persistEmiSchedule(loan, disbursementDate);
        loanStateMachine.validateTransition(LoanStatus.DISBURSED, LoanStatus.ACTIVE);
        loan.setLoanStatus(LoanStatus.ACTIVE);
        Loan savedLoan = loanRepository.save(loan);

        Long customerUserId = null;
        try {
            customerUserId = savedLoan.getCustomer().getUser().getUserId();
        } catch (Exception ignored) {
            log.debug("Customer user id not available for disbursement event. loanId={}", loanId);
        }

        LoanDisbursedEvent event = new LoanDisbursedEvent(
                savedLoan.getLoanId(), customerUserId, savedLoan.getLoanAmount(), disbursementDate);
        eventPublisher.publishEvent(event);
        outboxService.enqueue("LOAN", String.valueOf(savedLoan.getLoanId()), "LoanDisbursed", event);

        log.info("Loan disbursed successfully. loanId={}", loanId);
        return loanMapper.toResponseDto(savedLoan);
    }

    private void persistEmiSchedule(Loan loan, LocalDate disbursementDate) {
        List<EmiSchedule> existing = emiScheduleRepository
                .findByLoanLoanIdOrderByInstallmentNumberAsc(loan.getLoanId());
        if (!existing.isEmpty()) {
            emiScheduleRepository.deleteAll(existing);
        }

        BigDecimal balance = loan.getLoanAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal normalEmi = loan.getEmi().setScale(2, RoundingMode.HALF_UP);
        int tenure = loan.getTenureMonths();
        List<EmiSchedule> schedules = new ArrayList<>(tenure);

        for (int month = 1; month <= tenure; month++) {
            BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principal;
            BigDecimal installmentAmount;

            // The final installment absorbs paise-level rounding so principal closes exactly at zero.
            if (month == tenure) {
                principal = balance;
                installmentAmount = principal.add(interest).setScale(2, RoundingMode.HALF_UP);
            } else {
                principal = normalEmi.subtract(interest).setScale(2, RoundingMode.HALF_UP);
                if (principal.compareTo(balance) > 0) {
                    principal = balance;
                }
                installmentAmount = normalEmi;
            }

            balance = balance.subtract(principal).setScale(2, RoundingMode.HALF_UP);
            schedules.add(EmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(month)
                    .dueDate(disbursementDate.plusMonths(month))
                    .emiAmount(installmentAmount)
                    .principalComponent(principal)
                    .interestComponent(interest)
                    .outstandingPrincipalAfter(balance)
                    .status(EmiStatus.PENDING)
                    .build());
        }

        emiScheduleRepository.saveAll(schedules);
        log.info("EMI schedule persisted. loanId={}, installments={}", loan.getLoanId(), schedules.size());
    }
}
