package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.LoginRequestDto;
import com.sudhanshu.loanmanagement.dto.LoginResponseDto;
import com.sudhanshu.loanmanagement.security.CustomUserDetailsService;
import com.sudhanshu.loanmanagement.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequestDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(dto.getUsername());

        String token =
                jwtService.generateToken(userDetails);

        LoginResponseDto response =
                LoginResponseDto.builder()
                        .username(userDetails.getUsername())
                        .role(userDetails.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority())
                        .token(token)
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
}