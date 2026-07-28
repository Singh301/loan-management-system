package com.sudhanshu.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    private long totalLoans;

    private long pendingLoans;

    private long approvedLoans;

    private long rejectedLoans;

    private BigDecimal totalApprovedAmount;

}