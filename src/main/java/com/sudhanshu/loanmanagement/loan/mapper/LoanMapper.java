package com.sudhanshu.loanmanagement.loan.mapper;

import com.sudhanshu.loanmanagement.loan.dto.EmiScheduleResponseDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponseDto toResponseDto(Loan loan) {
        if (loan == null) {
            return null;
        }

        return LoanResponseDto.builder()
                .loanId(loan.getLoanId())
                .customerId(loan.getCustomer() != null ? loan.getCustomer().getCustomerId() : null)
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emi(loan.getEmi())
                .loanStatus(loan.getLoanStatus())
                .applicationDate(loan.getApplicationDate())
                .remarks(loan.getRemarks())
                .outstandingPrincipal(loan.getOutstandingPrincipal())
                .paidInstallments(loan.getPaidInstallments())
                .remainingInstallments(loan.getRemainingInstallments())
                .build();
    }

    public EmiScheduleResponseDto toEmiDto(EmiSchedule schedule) {
        if (schedule == null) {
            return null;
        }

        return EmiScheduleResponseDto.builder()
                .scheduleId(schedule.getScheduleId())
                .emiNumber(schedule.getInstallmentNumber())
                .dueDate(schedule.getDueDate())
                .emiAmount(schedule.getEmiAmount())
                .principalAmount(schedule.getPrincipalComponent())
                .interestAmount(schedule.getInterestComponent())
                .remainingBalance(schedule.getOutstandingPrincipalAfter())
                .status(schedule.getStatus())
                .paidDate(schedule.getPaidDate())
                .amountPaid(schedule.getAmountPaid())
                .lateFee(schedule.getLateFee())
                .build();
    }
}
