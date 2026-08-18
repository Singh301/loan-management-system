package com.sudhanshu.loanmanagement.user.dto;

import com.sudhanshu.loanmanagement.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "User Request",
        description = "Request payload used to register a new user."
)
public class UserRequestDto {

    @Schema(
            description = "Full name of the user",
            example = "Sudhanshu Kumar Singh"
    )
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(
            description = "Unique username used for login",
            example = "sudhanshu"
    )
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(
            description = "Email address of the user",
            example = "sudhanshu@gmail.com"
    )
    @Email(message = "Invalid email format")
    private String email;

    @Schema(
            description = "Password for the user account",
            example = "Admin@123"
    )
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(
            description = "Role assigned to the user",
            example = "ROLE_CUSTOMER",
            allowableValues = {
                    "ROLE_ADMIN",
                    "ROLE_MANAGER",
                    "ROLE_CUSTOMER"
            }
    )
    private Role role;

}




