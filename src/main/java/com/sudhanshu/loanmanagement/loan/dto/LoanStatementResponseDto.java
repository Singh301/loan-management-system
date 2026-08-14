package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Loan Statement Response",
        description = "Detailed loan statement including customer details, loan information, repayment summary and repayment history."
)
public class LoanStatementResponseDto {

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
            description = "Customer full name",
            example = "Sudhanshu Kumar Singh"
    )
    private String customerName;

    @Schema(
            description = "Customer email address",
            example = "sudhanshu@gmail.com"
    )
    private String email;

    @Schema(
            description = "Customer mobile number",
            example = "9876543210"
    )
    private String mobileNumber;

    @Schema(
            description = "Loan type",
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
            description = "Current loan status",
            example = "ACTIVE",
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
            description = "Approved loan amount",
            example = "1500000.00"
    )
    private BigDecimal loanAmount;

    @Schema(
            description = "Annual interest rate (%)",
            example = "8.50"
    )
    private BigDecimal interestRate;

    @Schema(
            description = "Loan tenure in months",
            example = "240"
    )
    private Integer tenureMonths;

    @Schema(
            description = "Monthly EMI amount",
            example = "13017.45"
    )
    private BigDecimal emi;

    @Schema(
            description = "Outstanding principal balance",
            example = "1184500.50"
    )
    private BigDecimal outstandingPrincipal;

    @Schema(
            description = "Number of installments already paid",
            example = "24"
    )
    private Integer paidInstallments;

    @Schema(
            description = "Remaining installments",
            example = "216"
    )
    private Integer remainingInstallments;

    @Schema(
            description = "Total amount paid so far",
            example = "312418.80"
    )
    private BigDecimal totalAmountPaid;

    @Schema(
            description = "Total principal repaid",
            example = "215499.35"
    )
    private BigDecimal totalPrincipalPaid;

    @Schema(
            description = "Total interest paid",
            example = "96919.45"
    )
    private BigDecimal totalInterestPaid;

    @Schema(
            description = "Complete repayment history for this loan"
    )
    private List<LoanRepaymentResponseDto> repayments;

}




