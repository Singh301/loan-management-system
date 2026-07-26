package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDto {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;

    private Role role;

}