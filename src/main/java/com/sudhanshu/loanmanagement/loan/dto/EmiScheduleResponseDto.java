package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "EMI Schedule Response",
        description = "Represents the details of a single EMI installment in the loan repayment schedule."
)
public class EmiScheduleResponseDto {

    @Schema(description = "Unique Schedule ID (from database)", example = "101")
    private Long scheduleId;

    @Schema(description = "EMI installment number", example = "1")
    private Integer emiNumber;

    @Schema(description = "Due date of this EMI", example = "2026-09-15")
    private LocalDate dueDate;

    @Schema(description = "Principal amount of this EMI", example = "8450.75")
    private BigDecimal principalAmount;

    @Schema(description = "Interest amount of this EMI", example = "2050.25")
    private BigDecimal interestAmount;

    @Schema(description = "Total EMI amount", example = "10501.00")
    private BigDecimal emiAmount;

    @Schema(description = "Outstanding loan balance after this EMI", example = "491549.25")
    private BigDecimal remainingBalance;

    @Schema(description = "Status of this EMI", example = "PENDING")
    private EmiStatus status;

    @Schema(description = "Date when this EMI was paid", example = "2026-09-14")
    private LocalDate paidDate;

    @Schema(description = "Actual amount paid for this installment", example = "10501.00")
    private BigDecimal amountPaid;

    @Schema(description = "Late fee applied on this installment (if any)", example = "500.00")
    private BigDecimal lateFee;
}