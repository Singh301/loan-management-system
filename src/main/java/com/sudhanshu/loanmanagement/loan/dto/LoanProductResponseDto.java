package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanProductResponseDto {

    private Long productId;
    private String productCode;
    private String productName;
    private LoanType loanType;
    private BigDecimal interestRate;
    private Integer minTenureMonths;
    private Integer maxTenureMonths;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal processingFeePercent;
    private BigDecimal lateFeeAmount;
    private Boolean active;
    private LocalDateTime createdAt;
}