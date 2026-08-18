package com.sudhanshu.loanmanagement.loan.event.listener;

import com.sudhanshu.loanmanagement.audit.service.AuditService;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.event.LoanCreatedEvent;
import com.sudhanshu.loanmanagement.loan.event.LoanDisbursedEvent;
import com.sudhanshu.loanmanagement.loan.event.LoanStatusChangedEvent;
import com.sudhanshu.loanmanagement.metrics.LoanMetrics;
import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles side-effects (notifications + audit + metrics) after successful loan transactions.
 * Uses AFTER_COMMIT so failures here never roll back the business transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoanEventListener {

    private final NotificationService notificationService;
    private final AuditService auditService;
    private final LoanMetrics loanMetrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanCreated(LoanCreatedEvent event) {
        log.info("Handling LoanCreatedEvent. loanId={}", event.loanId());
        loanMetrics.incrementApplied();
        try {
            auditService.saveAudit(
                    "SYSTEM",
                    "LOAN",
                    "CREATE",
                    "Loan created. Loan ID: " + event.loanId()
                            + ", Customer ID: " + event.customerId()
                            + ", Type: " + event.loanType()
                            + ", Amount: " + event.amount()
            );
        } catch (Exception e) {
            log.warn("Failed to audit loan creation. loanId={}", event.loanId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanStatusChanged(LoanStatusChangedEvent event) {
        log.info("Handling LoanStatusChangedEvent. loanId={}, {} -> {}",
                event.loanId(), event.previousStatus(), event.newStatus());

        if (event.newStatus() == LoanStatus.APPROVED) {
            loanMetrics.incrementApproved();
        } else if (event.newStatus() == LoanStatus.REJECTED) {
            loanMetrics.incrementRejected();
        }

        try {
            auditService.saveAudit(
                    event.performedBy() != null ? event.performedBy() : "SYSTEM",
                    "LOAN",
                    event.newStatus().name(),
                    "Loan status changed from " + event.previousStatus()
                            + " to " + event.newStatus()
                            + ". Loan ID: " + event.loanId()
            );
        } catch (Exception e) {
            log.warn("Failed to audit status change. loanId={}", event.loanId(), e);
        }

        if (event.customerUserId() == null) {
            return;
        }

        try {
            if (event.newStatus() == LoanStatus.APPROVED) {
                notificationService.sendNotification(
                        event.customerUserId(),
                        "Loan Approved",
                        "Your loan application (ID: " + event.loanId()
                                + ") has been approved successfully.",
                        Notification.NotificationType.IN_APP,
                        "LOAN",
                        event.loanId()
                );
            } else if (event.newStatus() == LoanStatus.REJECTED) {
                notificationService.sendNotification(
                        event.customerUserId(),
                        "Loan Rejected",
                        "Your loan application (ID: " + event.loanId()
                                + ") has been rejected. Reason: "
                                + (event.remarks() != null ? event.remarks() : "No reason provided"),
                        Notification.NotificationType.IN_APP,
                        "LOAN",
                        event.loanId()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send status notification. loanId={}", event.loanId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanDisbursed(LoanDisbursedEvent event) {
        log.info("Handling LoanDisbursedEvent. loanId={}", event.loanId());
        loanMetrics.incrementDisbursed();

        try {
            auditService.saveAudit(
                    "SYSTEM",
                    "LOAN",
                    "DISBURSED",
                    "Loan disbursed. Loan ID: " + event.loanId()
                            + ", Amount: " + event.amount()
                            + ", Date: " + event.disbursementDate()
            );
        } catch (Exception e) {
            log.warn("Failed to audit disbursement. loanId={}", event.loanId(), e);
        }

        if (event.customerUserId() == null) {
            return;
        }

        try {
            notificationService.sendNotification(
                    event.customerUserId(),
                    "Loan Disbursed",
                    "Your loan (ID: " + event.loanId()
                            + ") has been disbursed successfully. EMI schedule is now active.",
                    Notification.NotificationType.IN_APP,
                    "LOAN",
                    event.loanId()
            );
        } catch (Exception e) {
            log.warn("Failed to send disbursement notification. loanId={}", event.loanId(), e);
        }
    }
}
