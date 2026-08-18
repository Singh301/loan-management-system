package com.sudhanshu.loanmanagement.loan.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CollateralResponseDto {
    private Long collateralId;
    private Long loanId;
    private String collateralType;
    private String description;
    private BigDecimal estimatedValue;
    private String ownershipProof;
    private LocalDateTime createdAt;
}