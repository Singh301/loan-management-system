package com.sudhanshu.loanmanagement.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Loan Statistics Response",
        description = "Overall loan statistics and financial summary."
)
public class LoanStatisticsResponseDto {

    @Schema(
            description = "Total number of loan applications",
            example = "500"
    )
    private long totalLoans;

    @Schema(
            description = "Total number of approved loans",
            example = "350"
    )
    private long approvedLoans;

    @Schema(
            description = "Total number of pending loan applications",
            example = "100"
    )
    private long pendingLoans;

    @Schema(
            description = "Total number of rejected loan applications",
            example = "50"
    )
    private long rejectedLoans;

    @Schema(
            description = "Total loan amount requested",
            example = "350000000.00"
    )
    private BigDecimal totalLoanAmount;

    @Schema(
            description = "Total approved loan amount",
            example = "280000000.00"
    )
    private BigDecimal approvedLoanAmount;

    @Schema(
            description = "Average loan amount",
            example = "700000.00"
    )
    private BigDecimal averageLoanAmount;

    @Schema(
            description = "Highest loan amount approved/requested",
            example = "5000000.00"
    )
    private BigDecimal highestLoanAmount;

    @Schema(
            description = "Lowest loan amount approved/requested",
            example = "25000.00"
    )
    private BigDecimal lowestLoanAmount;

}




