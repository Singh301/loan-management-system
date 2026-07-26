package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(
            @Valid @RequestBody UserRequestDto dto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User registered successfully.")
                .data(userService.registerUser(dto))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}