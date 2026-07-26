package com.sudhanshu.loanmanagement.dto;

import com.sudhanshu.loanmanagement.entity.Role;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;

    private String fullName;

    private String username;

    private String email;

    private Role role;

    private Boolean enabled;

}