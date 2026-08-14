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
        name = "Loan Foreclosure Response",
        description = "Represents the foreclosure settlement details of a loan."
)
public class LoanForeclosureResponseDto {

    @Schema(
            description = "Unique loan identifier",
            example = "101"
    )
    private Long loanId;

    @Schema(
            description = "Outstanding principal amount remaining on the loan",
            example = "425000.00"
    )
    private BigDecimal remainingPrincipal;

    @Schema(
            description = "Estimated interest amount saved due to early loan closure",
            example = "28500.75"
    )
    private BigDecimal interestSaved;

    @Schema(
            description = "Applicable foreclosure or prepayment charges",
            example = "2500.00"
    )
    private BigDecimal foreclosureCharges;

    @Schema(
            description = "Final amount required to settle the loan completely",
            example = "427500.00"
    )
    private BigDecimal totalSettlementAmount;

}




