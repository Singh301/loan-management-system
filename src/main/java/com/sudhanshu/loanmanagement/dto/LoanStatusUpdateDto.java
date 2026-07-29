package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusUpdateDto {

    @NotNull
    private LoanStatus loanStatus;

    private String remarks;

}