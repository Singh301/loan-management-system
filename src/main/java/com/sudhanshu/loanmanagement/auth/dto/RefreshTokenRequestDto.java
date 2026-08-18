package com.sudhanshu.loanmanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "Refresh Token Request",
        description = "Request payload used to generate a new JWT access token using a valid refresh token."
)
public class RefreshTokenRequestDto {

    @Schema(
            description = "Valid refresh token issued during login",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.refreshTokenExample"
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

}




