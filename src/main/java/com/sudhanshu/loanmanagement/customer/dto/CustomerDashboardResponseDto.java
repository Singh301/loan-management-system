package com.sudhanshu.loanmanagement.customer.dto;

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
        name = "Customer Dashboard Response",
        description = "Dashboard statistics related to customers and their loans."
)
public class CustomerDashboardResponseDto {

    @Schema(
            description = "Total number of registered customers",
            example = "250"
    )
    private Long totalCustomers;

    @Schema(
            description = "Number of customers who have at least one loan",
            example = "180"
    )
    private Long customersWithLoans;

    @Schema(
            description = "Number of customers without any loan",
            example = "70"
    )
    private Long customersWithoutLoans;

    @Schema(
            description = "Total number of loans across all customers",
            example = "320"
    )
    private Long totalLoans;

    @Schema(
            description = "Total amount of all loan applications",
            example = "245000000.00"
    )
    private BigDecimal totalLoanAmount;

    @Schema(
            description = "Total amount of approved loans",
            example = "198500000.00"
    )
    private BigDecimal totalApprovedLoanAmount;

}




