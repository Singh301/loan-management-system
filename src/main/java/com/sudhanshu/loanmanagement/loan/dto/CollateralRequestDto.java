package com.sudhanshu.loanmanagement.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CollateralRequestDto {
    @NotBlank
    private String collateralType;
    private String description;
    @NotNull
    private BigDecimal estimatedValue;
    private String ownershipProof;
}