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
        name = "Loan Monthly Report",
        description = "Represents monthly loan statistics including total loans and total sanctioned amount."
)
public class LoanMonthlyReportDto {

    @Schema(
            description = "Report year",
            example = "2026"
    )
    private Integer year;

    @Schema(
            description = "Report month (1 = January, 12 = December)",
            example = "8"
    )
    private Integer month;

    @Schema(
            description = "Total number of loan applications in the month",
            example = "125"
    )
    private Long loanCount;

    @Schema(
            description = "Total loan amount for the month",
            example = "98500000.00"
    )
    private BigDecimal totalLoanAmount;

}




