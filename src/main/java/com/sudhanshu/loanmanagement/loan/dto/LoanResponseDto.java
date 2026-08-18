package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(
        name = "Loan Response",
        description = "Loan details returned by Loan Management APIs."
)
public class LoanResponseDto {

    @Schema(
            description = "Unique loan identifier",
            example = "101"
    )
    private Long loanId;

    @Schema(
            description = "Customer identifier",
            example = "1"
    )
    private Long customerId;

    @Schema(
            description = "Type of loan",
            example = "HOME",
            allowableValues = {
                    "HOME",
                    "PERSONAL",
                    "CAR",
                    "EDUCATION",
                    "BUSINESS",
                    "GOLD"
            }
    )
    private LoanType loanType;

    @Schema(
            description = "Approved loan amount",
            example = "500000.00"
    )
    private BigDecimal loanAmount;

    @Schema(
            description = "Annual interest rate (%)",
            example = "9.50"
    )
    private BigDecimal interestRate;

    @Schema(
            description = "Loan tenure in months",
            example = "60"
    )
    private Integer tenureMonths;

    @Schema(
            description = "Monthly EMI amount",
            example = "10501.25"
    )
    private BigDecimal emi;

    @Schema(
            description = "Current loan status",
            example = "APPROVED",
            allowableValues = {
                    "PENDING",
                    "APPROVED",
                    "REJECTED",
                    "ACTIVE",
                    "CLOSED"
            }
    )
    private LoanStatus loanStatus;

    @Schema(
            description = "Loan application date",
            example = "2026-08-07"
    )
    private LocalDate applicationDate;

    @Schema(
            description = "Remarks provided during loan processing",
            example = "Home renovation loan approved."
    )
    private String remarks;

    @Schema(
            description = "Remaining principal amount",
            example = "425000.00"
    )
    private BigDecimal outstandingPrincipal;

    @Schema(
            description = "Number of installments already paid",
            example = "8"
    )
    private Integer paidInstallments;

    @Schema(
            description = "Number of installments remaining",
            example = "52"
    )
    private Integer remainingInstallments;
}




