package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        name = "Loan Request",
        description = "Request payload used to apply for or update a loan."
)
public class LoanRequestDto {

    @Schema(
            description = "Unique customer ID",
            example = "1"
    )
    @NotNull(message = "Customer ID is required.")
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
    @NotNull(message = "Loan type is required.")
    private LoanType loanType;

    @Schema(
            description = "Requested loan amount",
            example = "500000.00"
    )
    @NotNull(message = "Loan amount is required.")
    @DecimalMin(value = "1000.00", message = "Loan amount must be at least 1000.")
    private BigDecimal loanAmount;

    @Schema(
            description = "Annual interest rate (percentage)",
            example = "9.50"
    )
    @NotNull(message = "Interest rate is required.")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0.")
    private BigDecimal interestRate;

    @Schema(
            description = "Loan tenure in months",
            example = "60"
    )
    @NotNull(message = "Loan tenure is required.")
    @Positive(message = "Loan tenure must be positive.")
    private Integer tenureMonths;

    @Schema(
            description = "Additional remarks for the loan application",
            example = "Home renovation loan."
    )
    private String remarks;

}




