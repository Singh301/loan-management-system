package com.sudhanshu.loanmanagement.repayment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Loan Repayment Response",
        description = "Represents a loan repayment transaction and its details."
)
public class LoanRepaymentResponseDto {

    @Schema(
            description = "Unique repayment identifier",
            example = "1001"
    )
    private Long repaymentId;

    @Schema(
            description = "Associated loan identifier",
            example = "101"
    )
    private Long loanId;

    @Schema(
            description = "Total amount paid in this repayment",
            example = "15000.00"
    )
    private BigDecimal amountPaid;

    @Schema(
            description = "Portion of the payment applied to the principal amount",
            example = "12000.00"
    )
    private BigDecimal principalPaid;

    @Schema(
            description = "Portion of the payment applied to interest",
            example = "3000.00"
    )
    private BigDecimal interestPaid;

    @Schema(
            description = "Outstanding principal after this repayment",
            example = "438000.00"
    )
    private BigDecimal remainingPrincipal;

    @Schema(
            description = "Date and time when the payment was made",
            example = "2026-08-07T14:30:15"
    )
    private LocalDateTime paymentDate;

    @Schema(
            description = "Mode of payment",
            example = "UPI"
    )
    private String paymentMode;

    @Schema(
            description = "Payment transaction reference number",
            example = "UPI20260807123456"
    )
    private String transactionReference;

    @Schema(
            description = "Additional remarks for the repayment",
            example = "Monthly EMI paid successfully."
    )
    private String remarks;
}




