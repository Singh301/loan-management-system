package com.sudhanshu.loanmanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "User Response",
        description = "User information returned by User Management APIs."
)
public class UserResponseDto {

    @Schema(
            description = "Unique user identifier",
            example = "1"
    )
    private Long userId;

    @Schema(
            description = "Username used for login",
            example = "sudhanshu"
    )
    private String username;

    @Schema(
            description = "Full name of the user",
            example = "Sudhanshu Kumar Singh"
    )
    private String fullName;

    @Schema(
            description = "Registered email address",
            example = "sudhanshu@gmail.com"
    )
    private String email;

    @Schema(
            description = "Assigned system role",
            example = "ROLE_ADMIN"
    )
    private String role;

    @Schema(
            description = "User account status",
            example = "true"
    )
    private Boolean enabled;
}




