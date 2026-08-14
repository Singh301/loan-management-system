package com.sudhanshu.loanmanagement.repayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRepaymentRequestDto {

    @NotNull
    private Long loanId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amountPaid;

    @NotBlank
    private String paymentMode;

    @NotBlank
    private String transactionReference;

    private String remarks;

    // Optional: If you want to pay a specific installment
    private Integer installmentNumber;

}




