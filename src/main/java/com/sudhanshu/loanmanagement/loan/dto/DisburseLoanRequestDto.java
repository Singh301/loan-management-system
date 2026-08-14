package com.sudhanshu.loanmanagement.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DisburseLoanRequestDto {

    @NotNull
    private LocalDate disbursementDate;

    private String remarks;
}