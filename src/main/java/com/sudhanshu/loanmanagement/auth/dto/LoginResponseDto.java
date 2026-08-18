package com.sudhanshu.loanmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Login Response",
        description = "Response returned after successful user authentication."
)
public class LoginResponseDto {

    @Schema(
            description = "Authenticated username",
            example = "sudhanshu"
    )
    private String username;

    @Schema(
            description = "Authenticated user's role",
            example = "ROLE_ADMIN"
    )
    private String role;

    @Schema(
            description = "JWT Access Token used for authenticated API requests",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdWRoYW5zaHUifQ..."
    )
    private String token;

    @Schema(
            description = "Login status message",
            example = "Login Successful"
    )
    private String message;

    @Schema(
            description = "Refresh token used to generate a new access token after expiration",
            example = "8d42bc1e-78ab-4d6f-9c9d-fc7a12345678"
    )
    private String refreshToken;
}




