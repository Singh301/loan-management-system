package com.sudhanshu.loanmanagement.document.dto;

import com.sudhanshu.loanmanagement.document.entity.DocumentVerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentVerifyRequestDto {
    @NotNull
    private DocumentVerificationStatus status;
    private String rejectionReason;
}