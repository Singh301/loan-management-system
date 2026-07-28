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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(
        name = "Loan Management",
        description = "APIs for managing loan applications, loan approval, loan search and loan updates."
)
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Apply for a new loan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Loan application submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @Operation(summary = "Get all loans")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loans fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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

    @Operation(summary = "Get loan by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loan found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Loan not found")
    })
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

    @Operation(summary = "Get all loans of a customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer loans fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
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

    @Operation(summary = "Update loan details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loan updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Loan already processed")
    })
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

    @Operation(summary = "Approve or Reject a loan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loan status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Loan already processed")
    })
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

    @Operation(summary = "Get loans with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loans fetched successfully")
    })
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

    @Operation(summary = "Get loans by status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loans fetched successfully")
    })
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

    @Operation(summary = "Get loans by loan type")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loans fetched successfully")
    })
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