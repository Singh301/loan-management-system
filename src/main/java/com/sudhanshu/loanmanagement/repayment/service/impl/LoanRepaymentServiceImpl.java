package com.sudhanshu.loanmanagement.repayment.service.impl;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;
import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentRequestDto;
import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.repayment.entity.LoanRepayment;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repayment.repository.LoanRepaymentRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.audit.service.AuditService;
import com.sudhanshu.loanmanagement.repayment.service.LoanRepaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.repository.EmiScheduleRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoanRepaymentServiceImpl
        implements LoanRepaymentService {

    private final LoanRepository loanRepository;

    private final LoanRepaymentRepository loanRepaymentRepository;

    private final AuditService auditService;

    private final EmiScheduleRepository emiScheduleRepository;

    private final NotificationService notificationService;


    @Override
    @Transactional
    public LoanRepaymentResponseDto repayLoan(LoanRepaymentRequestDto dto) {

        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + dto.getLoanId()));

        // Only ACTIVE or OVERDUE loans can accept payments
        if (loan.getLoanStatus() != LoanStatus.ACTIVE && loan.getLoanStatus() != LoanStatus.OVERDUE) {
            throw new IllegalStateException("Payment can only be made for ACTIVE or OVERDUE loans. Current status: " + loan.getLoanStatus());
        }

        // Get next pending/overdue EMI
        List<EmiSchedule> pendingEmis = emiScheduleRepository
                .findPendingOrOverdueByLoanId(loan.getLoanId());

        if (pendingEmis.isEmpty()) {
            throw new IllegalStateException("No pending EMIs found for this loan.");
        }

        EmiSchedule currentEmi = pendingEmis.get(0);   // Take the earliest one

        BigDecimal paymentAmount = dto.getAmountPaid();
        BigDecimal remainingPayment = paymentAmount;

        // 1. First pay Late Fee (if any)
        BigDecimal lateFee = currentEmi.getLateFee() != null ? currentEmi.getLateFee() : BigDecimal.ZERO;
        BigDecimal lateFeePaid = BigDecimal.ZERO;

        if (lateFee.compareTo(BigDecimal.ZERO) > 0 && remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
            lateFeePaid = remainingPayment.min(lateFee);
            remainingPayment = remainingPayment.subtract(lateFeePaid);
            currentEmi.setLateFee(lateFee.subtract(lateFeePaid));
        }

        // 2. Pay Interest Component
        BigDecimal interestPaid = BigDecimal.ZERO;
        if (remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
            interestPaid = remainingPayment.min(currentEmi.getInterestComponent());
            remainingPayment = remainingPayment.subtract(interestPaid);
        }

        // 3. Pay Principal Component
        BigDecimal principalPaid = BigDecimal.ZERO;
        if (remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
            principalPaid = remainingPayment.min(currentEmi.getPrincipalComponent());
            remainingPayment = remainingPayment.subtract(principalPaid);
        }

        // Update EMI Schedule
        BigDecimal totalPaidForThisEmi = lateFeePaid.add(interestPaid).add(principalPaid);
        currentEmi.setAmountPaid(
                (currentEmi.getAmountPaid() != null ? currentEmi.getAmountPaid() : BigDecimal.ZERO)
                        .add(totalPaidForThisEmi)
        );
        currentEmi.setPaidDate(LocalDate.now());

        // Check if EMI is fully paid
        BigDecimal expectedTotal = currentEmi.getEmiAmount()
                .add(currentEmi.getLateFee() != null ? currentEmi.getLateFee() : BigDecimal.ZERO);

        if (currentEmi.getAmountPaid().compareTo(expectedTotal) >= 0) {
            currentEmi.setStatus(EmiStatus.PAID);
            loan.setPaidInstallments(loan.getPaidInstallments() + 1);
            loan.setRemainingInstallments(loan.getRemainingInstallments() - 1);
        } else {
            currentEmi.setStatus(EmiStatus.PARTIALLY_PAID);
        }

        emiScheduleRepository.save(currentEmi);

        // Update Loan outstanding principal
        loan.setOutstandingPrincipal(
                loan.getOutstandingPrincipal().subtract(principalPaid).max(BigDecimal.ZERO)
        );

        // Update next due date
        if (currentEmi.getStatus() == EmiStatus.PAID && !pendingEmis.isEmpty() && pendingEmis.size() > 1) {
            loan.setNextDueDate(pendingEmis.get(1).getDueDate());
        }

        // Auto close loan if fully paid
        if (loan.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0
                || loan.getRemainingInstallments() <= 0) {
            loan.setLoanStatus(LoanStatus.CLOSED);
            loan.setOutstandingPrincipal(BigDecimal.ZERO);
            loan.setRemainingInstallments(0);
        } else if (loan.getLoanStatus() == LoanStatus.OVERDUE) {
            // If was overdue and now paid current EMI, move back to ACTIVE
            boolean stillHasOverdue = pendingEmis.stream()
                    .anyMatch(e -> e.getStatus() == EmiStatus.OVERDUE && !e.getScheduleId().equals(currentEmi.getScheduleId()));
            if (!stillHasOverdue) {
                loan.setLoanStatus(LoanStatus.ACTIVE);
            }
        }

        loanRepository.save(loan);

        // Save repayment record
        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .amountPaid(paymentAmount)
                .principalPaid(principalPaid)
                .interestPaid(interestPaid)
                .remainingPrincipal(loan.getOutstandingPrincipal())
                .paymentMode(dto.getPaymentMode())
                .transactionReference(dto.getTransactionReference())
                .remarks(dto.getRemarks())
                .paymentDate(LocalDateTime.now())
                .build();

        LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);

        // Audit
        auditService.saveAudit(
                "SYSTEM",
                "REPAYMENT",
                "CREATE",
                "Repayment of " + paymentAmount + " received for Loan ID: " + loan.getLoanId()
        );

        // ===== Notification Trigger =====
        try {
            notificationService.sendNotification(
                    loan.getCustomer().getUser().getUserId(),
                    "Payment Received",
                    "We have received your payment of ₹" + paymentAmount +
                            " for Loan ID: " + loan.getLoanId() + ". Thank you!",
                    Notification.NotificationType.IN_APP,
                    "REPAYMENT",
                    savedRepayment.getRepaymentId()
            );
        } catch (Exception e) {
            log.warn("Failed to send repayment notification for loanId={}", loan.getLoanId(), e);
        }

        return mapToResponse(savedRepayment);
    }

    private LoanRepaymentResponseDto mapToResponse(
            LoanRepayment repayment) {

        return LoanRepaymentResponseDto.builder()

                .repaymentId(
                        repayment.getRepaymentId())

                .loanId(
                        repayment.getLoan().getLoanId())

                .amountPaid(
                        repayment.getAmountPaid())

                .principalPaid(
                        repayment.getPrincipalPaid())

                .interestPaid(
                        repayment.getInterestPaid())

                .remainingPrincipal(
                        repayment.getRemainingPrincipal())

                .paymentDate(
                        repayment.getPaymentDate())

                .paymentMode(
                        repayment.getPaymentMode())

                .transactionReference(
                        repayment.getTransactionReference())

                .remarks(
                        repayment.getRemarks())

                .build();
    }

    @Override
    public List<LoanRepaymentResponseDto> getRepaymentHistory(
            Long loanId) {

        loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : "
                                        + loanId));

        return loanRepaymentRepository

                .findByLoanLoanIdOrderByPaymentDateDesc(
                        loanId)

                .stream()

                .map(this::mapToResponse)

                .toList();
    }



        }




