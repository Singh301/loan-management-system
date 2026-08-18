package com.sudhanshu.loanmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
        name = "Refresh Token Response",
        description = "Response returned after refreshing the JWT access token."
)
public class RefreshTokenResponseDto {

    @Schema(
            description = "New JWT access token",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdWRoYW5zaHUifQ..."
    )
    private String accessToken;

    @Schema(
            description = "Refresh token used for future access token generation",
            example = "8d42bc1e-78ab-4d6f-9c9d-fc7a12345678"
    )
    private String refreshToken;

    @Schema(
            description = "Authentication token type",
            example = "Bearer"
    )
    private String tokenType;
}




