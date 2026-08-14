package com.sudhanshu.loanmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "Login Request",
        description = "Request payload used to authenticate a user."
)
public class LoginRequestDto {

    @Schema(
            description = "Registered username",
            example = "admin"
    )
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(
            description = "User account password",
            example = "Admin@123"
    )
    @NotBlank(message = "Password is required")
    private String password;

}




