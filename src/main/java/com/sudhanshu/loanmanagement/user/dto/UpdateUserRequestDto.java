package com.sudhanshu.loanmanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Update User Request",
        description = "Request payload used to update an existing user's basic information."
)
public class UpdateUserRequestDto {

    @Schema(
            description = "Updated full name of the user",
            example = "Sudhanshu Kumar Singh"
    )
    @NotBlank(message = "Full name is required.")
    private String fullName;

    @Schema(
            description = "Updated email address of the user",
            example = "sudhanshu@gmail.com"
    )
    @Email(message = "Invalid email.")
    @NotBlank(message = "Email is required.")
    private String email;

}




