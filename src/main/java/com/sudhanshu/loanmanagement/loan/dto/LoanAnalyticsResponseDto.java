package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(
        name = "Loan Analytics Response",
        description = "Analytical summary of loans grouped by loan type."
)
public class LoanAnalyticsResponseDto {

    @Schema(
            description = "Loan category",
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
            description = "Total number of loans for this loan type",
            example = "125"
    )
    private Long totalLoans;

    @Schema(
            description = "Total loan amount for this loan type",
            example = "95000000.00"
    )
    private BigDecimal totalAmount;

    @Schema(
            description = "Average loan amount for this loan type",
            example = "760000.00"
    )
    private BigDecimal averageLoanAmount;

}




