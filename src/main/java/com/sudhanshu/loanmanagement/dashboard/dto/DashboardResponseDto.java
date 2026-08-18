package com.sudhanshu.loanmanagement.dashboard.dto;

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
        name = "Dashboard Response",
        description = "Overall dashboard summary showing loan statistics."
)
public class DashboardResponseDto {

    @Schema(
            description = "Total number of loan applications",
            example = "250"
    )
    private long totalLoans;

    @Schema(
            description = "Total number of pending loan applications",
            example = "35"
    )
    private long pendingLoans;

    @Schema(
            description = "Total number of approved loans",
            example = "180"
    )
    private long approvedLoans;

    @Schema(
            description = "Total number of rejected loans",
            example = "35"
    )
    private long rejectedLoans;

    @Schema(
            description = "Total approved loan amount",
            example = "125000000.00"
    )
    private BigDecimal totalApprovedAmount;

}




