package com.sudhanshu.loanmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto {

    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;
}