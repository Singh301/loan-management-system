package com.sudhanshu.loanmanagement.auth.controller;

import com.sudhanshu.loanmanagement.auth.dto.LoginRequestDto;
import com.sudhanshu.loanmanagement.auth.dto.LoginResponseDto;
import com.sudhanshu.loanmanagement.auth.dto.RefreshTokenRequestDto;
import com.sudhanshu.loanmanagement.auth.dto.RefreshTokenResponseDto;
import com.sudhanshu.loanmanagement.auth.entity.RefreshToken;
import com.sudhanshu.loanmanagement.auth.service.RefreshTokenService;
import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.security.CustomUserDetails;
import com.sudhanshu.loanmanagement.security.CustomUserDetailsService;
import com.sudhanshu.loanmanagement.security.JwtService;
import com.sudhanshu.loanmanagement.security.LoginAttemptService;
import com.sudhanshu.loanmanagement.security.TokenBlacklistService;
import com.sudhanshu.loanmanagement.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Tag(
        name = "Authentication APIs",
        description = "JWT Authentication, Refresh Token and Logout APIs"
)
@RestController
@RequestMapping(ApiConstants.API_V1 + "/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;

    @Operation(
            summary = "User Login",
            description = "Authenticate user and generate JWT Access Token and Refresh Token."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Login Successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "Invalid Username or Password"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "423", description = "Account temporarily locked")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequestDto dto) {

        if (loginAttemptService.isBlocked(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Account temporarily locked due to too many failed attempts. Try again later.")
                            .data(null)
                            .build());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsername(),
                            dto.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(dto.getUsername());
            int remaining = loginAttemptService.remainingAttempts(dto.getUsername());
            throw new BadCredentialsException(
                    remaining > 0
                            ? "Invalid username or password. Remaining attempts: " + remaining
                            : "Invalid username or password. Account is now locked.");
        }

        loginAttemptService.loginSucceeded(dto.getUsername());

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(dto.getUsername());

        String accessToken = jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        ((CustomUserDetails) userDetails).getUser()
                );

        LoginResponseDto response =
                LoginResponseDto.builder()
                        .username(userDetails.getUsername())
                        .role(userDetails.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority())
                        .token(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .message("Login Successful")
                        .build();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Authentication successful")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Refresh Access Token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(
            @Valid @RequestBody RefreshTokenRequestDto dto) {

        RefreshToken refreshToken =
                refreshTokenService.findByToken(dto.getRefreshToken());

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = jwtService.generateToken(userDetails);

        RefreshTokenResponseDto response =
                RefreshTokenResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Token refreshed successfully")
                        .data(response)
                        .build()
        );
    }

    @Operation(
            summary = "Logout",
            description = "Invalidate refresh token and blacklist current access token."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(Authentication authentication,
                                              HttpServletRequest request) {

        if (authentication != null
                && authentication.getPrincipal() instanceof CustomUserDetails details) {
            refreshTokenService.deleteByUser(details.getUser());
        }

        // Blacklist current access token so it cannot be reused
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Date expiry = jwtService.extractExpiration(token);
                tokenBlacklistService.blacklist(token, expiry.getTime());
            } catch (Exception ignored) {
                // token may already be invalid
            }
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Logout successful.")
                        .data(null)
                        .build()
        );
    }
}
