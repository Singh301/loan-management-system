package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> applyLoan(
            @Valid @RequestBody LoanRequestDto requestDto) {

        LoanResponseDto responseDto = loanService.applyLoan(requestDto);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan application submitted successfully.")
                .data(responseDto)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getAllLoans() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getAllLoans())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanById(
            @PathVariable Long loanId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan fetched successfully.")
                .data(loanService.getLoanById(loanId))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByCustomer(
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer loans fetched successfully.")
                .data(loanService.getLoansByCustomer(customerId))
                .build();

        return ResponseEntity.ok(response);
    }


}