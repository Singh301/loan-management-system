package com.sudhanshu.loanmanagement.loan.scheduler;

import com.sudhanshu.loanmanagement.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanScheduler {

    private final LoanService loanService;

    /**
     * Runs every day at 1:00 AM
     * Marks pending EMIs as OVERDUE and updates loan status
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueLoans() {
        log.info("Starting scheduled job: Mark Overdue Loans");
        try {
            loanService.markOverdueLoans();
            log.info("Scheduled job completed successfully");
        } catch (Exception e) {
            log.error("Error while marking overdue loans", e);
        }
    }
}