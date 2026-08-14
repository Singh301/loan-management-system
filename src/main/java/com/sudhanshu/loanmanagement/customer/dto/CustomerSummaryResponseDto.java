package com.sudhanshu.loanmanagement.customer.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
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
        name = "Customer Summary Response",
        description = "Summarized information about a customer and their loan portfolio."
)
public class CustomerSummaryResponseDto {

    @Schema(
            description = "Unique customer identifier",
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
            description = "Total number of loan applications",
            example = "6"
    )
    private Long totalLoans;

    @Schema(
            description = "Number of pending loan applications",
            example = "1"
    )
    private Long pendingLoans;

    @Schema(
            description = "Number of approved loans",
            example = "4"
    )
    private Long approvedLoans;

    @Schema(
            description = "Number of rejected loans",
            example = "1"
    )
    private Long rejectedLoans;

    @Schema(
            description = "Total loan amount applied by the customer",
            example = "1850000.00"
    )
    private BigDecimal totalLoanAmount;

    @Schema(
            description = "Total approved loan amount",
            example = "1500000.00"
    )
    private BigDecimal approvedLoanAmount;

    @Schema(
            description = "Latest loan status",
            example = "APPROVED",
            allowableValues = {
                    "PENDING",
                    "APPROVED",
                    "REJECTED",
                    "ACTIVE",
                    "CLOSED"
            }
    )
    private LoanStatus latestLoanStatus;

    @Schema(
            description = "Date of the latest loan application",
            example = "2026-08-07"
    )
    private LocalDate latestLoanDate;

}




