package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequestDto {

    @NotNull
    private Long customerId;

    @NotNull
    private LoanType loanType;

    @NotNull
    @DecimalMin("1000.00")
    private BigDecimal loanAmount;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal interestRate;

    @NotNull
    @Positive
    private Integer tenureMonths;

    private String remarks;

}