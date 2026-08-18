package com.sudhanshu.loanmanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Update User Role Request",
        description = "Request payload used to change the role of an existing user."
)
public class UpdateUserRoleRequestDto {

    @Schema(
            description = "Role to be assigned to the user",
            example = "ROLE_MANAGER",
            allowableValues = {
                    "ROLE_ADMIN",
                    "ROLE_MANAGER",
                    "ROLE_CUSTOMER"
            }
    )
    @NotBlank(message = "Role is required.")
    private String role;

}




