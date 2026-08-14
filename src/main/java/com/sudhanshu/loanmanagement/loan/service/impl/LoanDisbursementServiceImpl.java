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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sudhanshu.loanmanagement.outbox.OutboxService;
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
    public LoanResponseDto disburseLoan(Long loanId,
                                        DisburseLoanRequestDto request,
                                        String idempotencyKey) {

        log.info("Disbursement requested. loanId={}, idempotencyKey={}", loanId, idempotencyKey);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id : " + loanId));

        if (Boolean.TRUE.equals(loan.getDeleted())) {
            throw new ResourceNotFoundException("Loan not found with id : " + loanId);
        }

        // Idempotency: same key + already disbursed → return current state
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyKey.equals(loan.getDisbursementIdempotencyKey())
                    && (loan.getLoanStatus() == LoanStatus.DISBURSED
                    || loan.getLoanStatus() == LoanStatus.ACTIVE)) {
                log.info("Idempotent disbursement hit. Returning existing state. loanId={}", loanId);
                return loanMapper.toResponseDto(loan);
            }
        }

        loanStateMachine.validateTransition(loan.getLoanStatus(), LoanStatus.DISBURSED);

        if (loan.getEmi() == null || loan.getEmi().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "EMI must be calculated before disbursement. Approve the loan first.");
        }

        LocalDate disbursementDate = request.getDisbursementDate() != null
                ? request.getDisbursementDate()
                : LocalDate.now();

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
        }

        LoanDisbursedEvent disbursedEvent = new LoanDisbursedEvent(
                savedLoan.getLoanId(),
                customerUserId,
                savedLoan.getLoanAmount(),
                disbursementDate
        );
        eventPublisher.publishEvent(disbursedEvent);
        outboxService.enqueue("LOAN", String.valueOf(savedLoan.getLoanId()),
                "LoanDisbursed", disbursedEvent);

        log.info("Loan disbursed successfully. loanId={}", loanId);
        return loanMapper.toResponseDto(savedLoan);
    }

    private void persistEmiSchedule(Loan loan, LocalDate disbursementDate) {
        List<EmiSchedule> existing = emiScheduleRepository
                .findByLoanLoanIdOrderByInstallmentNumberAsc(loan.getLoanId());
        if (!existing.isEmpty()) {
            emiScheduleRepository.deleteAll(existing);
        }

        BigDecimal balance = loan.getLoanAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal emi = loan.getEmi();
        int tenure = loan.getTenureMonths();

        List<EmiSchedule> schedules = new ArrayList<>();

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
                    .dueDate(disbursementDate.plusMonths(month))
                    .emiAmount(emi)
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
