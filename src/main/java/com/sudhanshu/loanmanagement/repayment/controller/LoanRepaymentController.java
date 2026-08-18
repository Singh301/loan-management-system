package com.sudhanshu.loanmanagement.repayment.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentRequestDto;
import com.sudhanshu.loanmanagement.repayment.service.LoanRepaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/repayments")
@RequiredArgsConstructor
@Tag(
        name = "Loan Repayment Management",
        description = "APIs for loan repayment and repayment history."
)
@SecurityRequirement(name = "bearerAuth")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    @Operation(
            summary = "Repay Loan",
            description = "Processes a loan repayment for a customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan repayment successful"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid repayment request"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> repayLoan(
            @Valid @RequestBody LoanRepaymentRequestDto dto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan repayment successful.")
                .data(loanRepaymentService.repayLoan(dto))
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get Repayment History",
            description = "Returns complete repayment history for a loan."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Repayment history fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<ApiResponse> getRepaymentHistory(

            @Parameter(
                    description = "Loan ID",
                    example = "1"
            )
            @PathVariable Long loanId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Repayment history fetched successfully.")
                .data(loanRepaymentService.getRepaymentHistory(loanId))
                .build();

        return ResponseEntity.ok(response);
    }

}




