package com.sudhanshu.loanmanagement.loan.dto;

import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanProductRequestDto {

    @NotBlank
    private String productCode;

    @NotBlank
    private String productName;

    @NotNull
    private LoanType loanType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal interestRate;

    @NotNull
    @Min(1)
    private Integer minTenureMonths;

    @NotNull
    @Min(1)
    private Integer maxTenureMonths;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal minAmount;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal maxAmount;

    private BigDecimal processingFeePercent;

    private BigDecimal lateFeeAmount;
}