package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Customer Loan History Response",
        description = "Complete loan history and summary details of a customer."
)
public class CustomerLoanHistoryResponseDto {

    @Schema(
            description = "Unique customer identifier",
            example = "101"
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
    private long totalLoans;

    @Schema(
            description = "Number of approved loans",
            example = "4"
    )
    private long approvedLoans;

    @Schema(
            description = "Number of pending loans",
            example = "1"
    )
    private long pendingLoans;

    @Schema(
            description = "Number of rejected loans",
            example = "1"
    )
    private long rejectedLoans;

    @Schema(
            description = "Complete loan history of the customer"
    )
    private List<LoanResponseDto> loans;

}




