package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
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

    @PutMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> updateLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRequestDto requestDto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan updated successfully.")
                .data(loanService.updateLoan(loanId, requestDto))
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{loanId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> updateLoanStatus(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanStatusUpdateDto requestDto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan status updated successfully.")
                .data(loanService.updateLoanStatus(loanId, requestDto))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "loanId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansWithPagination(
                        page, size, sortBy, direction))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByStatus(
            @PathVariable LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansByStatus(
                        status, page, size))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{loanType}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByType(
            @PathVariable LoanType loanType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansByType(
                        loanType, page, size))
                .build();

        return ResponseEntity.ok(response);
    }


}