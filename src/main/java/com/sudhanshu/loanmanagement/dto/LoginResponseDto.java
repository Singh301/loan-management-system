package com.sudhanshu.loanmanagement.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String username;

    private String role;

    private String token;

    private String message;

}