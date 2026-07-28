package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanStatusUpdateDto {

    @NotNull
    private LoanStatus loanStatus;

    private String remarks;

}