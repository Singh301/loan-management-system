package com.sudhanshu.loanmanagement.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom business metrics for loan lifecycle.
 * Exposed via Actuator / Prometheus.
 */
@Component
public class LoanMetrics {

    private final Counter loansApplied;
    private final Counter loansApproved;
    private final Counter loansRejected;
    private final Counter loansDisbursed;
    private final Counter loansOverdue;
    private final Timer approvalTimer;

    public LoanMetrics(MeterRegistry registry) {
        this.loansApplied = Counter.builder("loan.applied")
                .description("Number of loan applications submitted")
                .register(registry);

        this.loansApproved = Counter.builder("loan.approved")
                .description("Number of loans approved")
                .register(registry);

        this.loansRejected = Counter.builder("loan.rejected")
                .description("Number of loans rejected")
                .register(registry);

        this.loansDisbursed = Counter.builder("loan.disbursed")
                .description("Number of loans disbursed")
                .register(registry);

        this.loansOverdue = Counter.builder("loan.overdue")
                .description("Number of loans marked overdue")
                .register(registry);

        this.approvalTimer = Timer.builder("loan.approval.duration")
                .description("Time taken for loan approval workflow")
                .register(registry);
    }

    public void incrementApplied() {
        loansApplied.increment();
    }

    public void incrementApproved() {
        loansApproved.increment();
    }

    public void incrementRejected() {
        loansRejected.increment();
    }

    public void incrementDisbursed() {
        loansDisbursed.increment();
    }

    public void incrementOverdue() {
        loansOverdue.increment();
    }

    public Timer.Sample startApprovalTimer() {
        return Timer.start();
    }

    public void stopApprovalTimer(Timer.Sample sample) {
        if (sample != null) {
            sample.stop(approvalTimer);
        }
    }
}
