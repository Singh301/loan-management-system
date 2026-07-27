package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanResponseDto {

    private Long loanId;

    private Long customerId;

    private LoanType loanType;

    private BigDecimal loanAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal emi;

    private LoanStatus loanStatus;

    private LocalDate applicationDate;

    private String remarks;

}