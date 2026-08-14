package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.domain.LoanStateMachine;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanApproval;
import com.sudhanshu.loanmanagement.loan.entity.LoanApproval.ApprovalStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.event.LoanStatusChangedEvent;
import com.sudhanshu.loanmanagement.loan.mapper.LoanMapper;
import com.sudhanshu.loanmanagement.loan.repository.LoanApprovalRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.loan.service.LoanApprovalService;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sudhanshu.loanmanagement.outbox.OutboxService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApprovalServiceImpl implements LoanApprovalService {

    private static final int MANAGER_LEVEL = 1;
    private static final int ADMIN_LEVEL = 2;

    private final LoanRepository loanRepository;
    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanStateMachine loanStateMachine;
    private final EmiCalculator emiCalculator;
    private final LoanMapper loanMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public LoanResponseDto updateLoanStatus(Long loanId,
                                            LoanStatusUpdateDto requestDto,
                                            Long approverUserId,
                                            String approverRole) {

        log.info("Loan status update requested. loanId={}, newStatus={}, approver={}",
                loanId, requestDto.getLoanStatus(), approverUserId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id : " + loanId));

        if (Boolean.TRUE.equals(loan.getDeleted())) {
            throw new ResourceNotFoundException("Loan not found with id : " + loanId);
        }

        if (!loan.getCustomer().isActive()) {
            throw new IllegalStateException(
                    "Loan cannot be processed because customer is inactive.");
        }

        LoanStatus targetStatus = requestDto.getLoanStatus();
        LoanStatus previousStatus = loan.getLoanStatus();

        if (targetStatus != LoanStatus.APPROVED && targetStatus != LoanStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Only APPROVED or REJECTED status can be set via approval API.");
        }

        if (targetStatus == LoanStatus.REJECTED
                && (requestDto.getRemarks() == null || requestDto.getRemarks().isBlank())) {
            throw new IllegalArgumentException("Remarks are mandatory when rejecting a loan.");
        }

        if (targetStatus == LoanStatus.APPROVED) {
            validateLoanForApproval(loan);
        }

        int level = resolveApprovalLevel(approverRole);

        if (targetStatus == LoanStatus.APPROVED) {
            handleApproval(loan, requestDto, approverUserId, level);
        } else {
            handleRejection(loan, requestDto, approverUserId, level);
        }

        Loan updatedLoan = loanRepository.save(loan);

        // Publish only when status actually changed (e.g. Admin final approval or rejection)
        if (updatedLoan.getLoanStatus() != previousStatus) {
            Long customerUserId = null;
            try {
                customerUserId = updatedLoan.getCustomer().getUser().getUserId();
            } catch (Exception ignored) {
                // user may not be loaded
            }

            LoanStatusChangedEvent statusEvent = new LoanStatusChangedEvent(
                    updatedLoan.getLoanId(),
                    customerUserId,
                    previousStatus,
                    updatedLoan.getLoanStatus(),
                    requestDto.getRemarks(),
                    String.valueOf(approverUserId)
            );
            eventPublisher.publishEvent(statusEvent);
            outboxService.enqueue("LOAN", String.valueOf(updatedLoan.getLoanId()),
                    "LoanStatusChanged", statusEvent);
        }

        log.info("Loan status updated successfully. loanId={}, status={}",
                loanId, updatedLoan.getLoanStatus());

        return loanMapper.toResponseDto(updatedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApproval> getApprovalHistory(Long loanId) {
        return loanApprovalRepository.findByLoanLoanIdOrderByLevelAsc(loanId);
    }

    private void handleApproval(Loan loan,
                                LoanStatusUpdateDto requestDto,
                                Long approverUserId,
                                int level) {

        if (loan.getLoanStatus() != LoanStatus.PENDING
                && loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new LoanAlreadyProcessedException("Loan has already been processed.");
        }

        LoanApproval approval = LoanApproval.builder()
                .loan(loan)
                .approverUserId(approverUserId)
                .level(level)
                .status(ApprovalStatus.APPROVED)
                .remarks(requestDto.getRemarks())
                .actionAt(LocalDateTime.now())
                .build();
        loanApprovalRepository.save(approval);

        // Manager approval is intermediate
        if (level == MANAGER_LEVEL) {
            log.info("Manager approval recorded for loanId={}. Waiting for Admin approval.",
                    loan.getLoanId());
            return;
        }

        // Admin final approval
        loanStateMachine.validateTransition(loan.getLoanStatus(), LoanStatus.APPROVED);

        loan.setLoanStatus(LoanStatus.APPROVED);
        if (requestDto.getRemarks() != null && !requestDto.getRemarks().isBlank()) {
            loan.setRemarks(requestDto.getRemarks());
        }

        loan.setEmi(emiCalculator.calculateEmi(
                loan.getLoanAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()));

        log.debug("EMI calculated. loanId={}, emi={}", loan.getLoanId(), loan.getEmi());
    }

    private void handleRejection(Loan loan,
                                 LoanStatusUpdateDto requestDto,
                                 Long approverUserId,
                                 int level) {

        loanStateMachine.validateTransition(loan.getLoanStatus(), LoanStatus.REJECTED);

        LoanApproval approval = LoanApproval.builder()
                .loan(loan)
                .approverUserId(approverUserId)
                .level(level)
                .status(ApprovalStatus.REJECTED)
                .remarks(requestDto.getRemarks())
                .actionAt(LocalDateTime.now())
                .build();
        loanApprovalRepository.save(approval);

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setRemarks(requestDto.getRemarks());
    }

    private void validateLoanForApproval(Loan loan) {
        if (loan.getLoanAmount() == null
                || loan.getInterestRate() == null
                || loan.getTenureMonths() == null) {
            throw new IllegalStateException("Loan details are incomplete.");
        }
        if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid loan amount.");
        }
        if (loan.getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid interest rate.");
        }
        if (loan.getTenureMonths() <= 0) {
            throw new IllegalArgumentException("Invalid loan tenure.");
        }
    }

    private int resolveApprovalLevel(String role) {
        if (role == null) {
            return MANAGER_LEVEL;
        }
        String normalized = role.toUpperCase().replace("ROLE_", "");
        if ("ADMIN".equals(normalized)) {
            return ADMIN_LEVEL;
        }
        return MANAGER_LEVEL;
    }
}
