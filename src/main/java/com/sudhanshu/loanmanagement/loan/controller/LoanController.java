package com.sudhanshu.loanmanagement.loan.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.PageResponse;
import com.sudhanshu.loanmanagement.loan.dto.*;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import com.sudhanshu.loanmanagement.loan.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import java.math.BigDecimal;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/loans")
@RequiredArgsConstructor
@Slf4j

@Tag(
        name = "Loan Management",
        description = "APIs for Loan Application, Approval, Search, Analytics, EMI Schedule, Foreclosure, Reports and Customer Loan History."
)

@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @Operation(
            summary = "Apply for a new loan",
            description = "Creates a new loan application for an existing customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Loan application submitted successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan request"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> applyLoan(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LoanRequestDto requestDto) {

        LoanResponseDto responseDto = loanService.applyLoan(requestDto);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan application submitted successfully.")
                .data(responseDto)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
            summary = "Get All Loans (Paginated)",
            description = "Returns paginated list of loans. Optionally filter by loan type."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loans fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getAllLoans(

            @Parameter(description = "Optional loan type filter", example = "CAR")
            @RequestParam(required = false) LoanType loanType,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field", example = "loanId")
            @RequestParam(defaultValue = "loanId") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String direction) {

        var result = loanService.getAllLoansPaginated(loanType, page, size, sortBy, direction);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(PageResponse.from(result))
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get Loan By ID",
            description = "Fetch a specific loan using its Loan ID."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found")
    })
    @GetMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanById(

            @Parameter(
                    description = "Loan ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long loanId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan fetched successfully.")
                .data(loanService.getLoanById(loanId))
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Get Loans By Customer",
            description = "Returns all loans belonging to a particular customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer loans fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByCustomer(

            @Parameter(
                    description = "Customer ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer loans fetched successfully.")
                .data(loanService.getLoansByCustomer(customerId))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get My Loans",
            description = "Returns all loans belonging to the currently authenticated customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer loans fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getMyLoans(
            Principal principal) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(
                        loanService.getMyLoans(
                                principal.getName()
                        )
                )
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get My Loan By ID",
            description = "Returns a specific loan of the currently authenticated customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found")
    })
    @GetMapping("/my-loans/{loanId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getMyLoan(

            @Parameter(
                    description = "Loan ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long loanId,

            Principal principal) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan fetched successfully.")
                .data(
                        loanService.getMyLoan(
                                loanId,
                                principal.getName()))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update Loan",
            description = "Updates an existing loan before approval or rejection."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan updated successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Loan already processed or invalid request"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found")
    })
    @PutMapping("/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> updateLoan(

            @Parameter(
                    description = "Loan ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long loanId,

            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LoanRequestDto requestDto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan updated successfully.")
                .data(loanService.updateLoan(loanId, requestDto))
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Search Loans",
            description = "Searches loans using customer name, email, loan type, loan status and loan amount range."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loans searched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameters"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> searchLoans(

            @Parameter(
                    description = "Customer name",
                    example = "Sudhanshu"
            )
            @RequestParam(required = false)
            String customerName,

            @Parameter(
                    description = "Customer email",
                    example = "sudhanshu@gmail.com"
            )
            @RequestParam(required = false)
            String email,

            @Parameter(
                    description = "Loan type",
                    example = "HOME"
            )
            @RequestParam(required = false)
            LoanType loanType,

            @Parameter(
                    description = "Loan status",
                    example = "PENDING"
            )
            @RequestParam(required = false)
            LoanStatus loanStatus,

            @Parameter(
                    description = "Minimum loan amount",
                    example = "100000"
            )
            @RequestParam(required = false)
            BigDecimal minAmount,

            @Parameter(
                    description = "Maximum loan amount",
                    example = "500000"
            )
            @RequestParam(required = false)
            BigDecimal maxAmount,

            @Parameter(
                    description = "Page number",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Page size",
                    example = "5"
            )
            @RequestParam(defaultValue = "5")
            int size,

            @Parameter(
                    description = "Field used for sorting",
                    example = "loanId"
            )
            @RequestParam(defaultValue = "loanId")
            String sortBy,

            @Parameter(
                    description = "Sort direction",
                    example = "asc"
            )
            @RequestParam(defaultValue = "asc")
            String direction) {

        log.info(
                "Search request received. customerName={}, email={}, loanType={}, loanStatus={}, minAmount={}, maxAmount={}",
                customerName,
                email,
                loanType,
                loanStatus,
                minAmount,
                maxAmount
        );

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans searched successfully.")
                .data(
                        loanService.searchLoans(
                                customerName,
                                email,
                                loanType,
                                loanStatus,
                                minAmount,
                                maxAmount,
                                page,
                                size,
                                sortBy,
                                direction
                        )
                )
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Approve or Reject Loan",
            description = "Approves or rejects a loan application."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan status updated successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Loan already processed or invalid request"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found")
    })
    @PatchMapping("/{loanId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> updateLoanStatus(

            @Parameter(
                    description = "Loan ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long loanId,

            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LoanStatusUpdateDto requestDto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan status updated successfully.")
                .data(loanService.updateLoanStatus(loanId, requestDto))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Loans With Pagination",
            description = "Returns paginated loan records."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loans fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansWithPagination(

            @Parameter(description = "Page Number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page Size", example = "10")
            @RequestParam(defaultValue = "5") int size,

            @Parameter(description = "Sort Field", example = "loanId")
            @RequestParam(defaultValue = "loanId") String sortBy,

            @Parameter(description = "Sort Direction", example = "asc")
            @RequestParam(defaultValue = "asc") String direction) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansWithPagination(
                        page,
                        size,
                        sortBy,
                        direction))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Loans By Status",
            description = "Returns loans filtered by loan status."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loans fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByStatus(

            @Parameter(
                    description = "Loan Status",
                    example = "APPROVED"
            )
            @PathVariable LoanStatus status,

            @Parameter(description = "Page Number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page Size", example = "5")
            @RequestParam(defaultValue = "5") int size) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansByStatus(
                        status,
                        page,
                        size))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Loans By Type",
            description = "Returns loans filtered by loan type."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loans fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/type/{loanType}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoansByType(

            @Parameter(
                    description = "Loan Type",
                    example = "HOME"
            )
            @PathVariable LoanType loanType,

            @Parameter(description = "Page Number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page Size", example = "5")
            @RequestParam(defaultValue = "5") int size) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loans fetched successfully.")
                .data(loanService.getLoansByType(
                        loanType,
                        page,
                        size))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Customer Loan History",
            description = "Returns complete loan history of a customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan history fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/customer/{customerId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getCustomerLoanHistory(

            @Parameter(
                    description = "Customer ID",
                    example = "1"
            )
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer loan history fetched successfully.")
                .data(loanService.getCustomerLoanHistory(customerId))
                .build();

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Loan Statistics",
            description = "Returns loan statistics for the dashboard."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan statistics fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanStatistics() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan statistics fetched successfully.")
                .data(loanService.getLoanStatistics())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Monthly Loan Report",
            description = "Returns monthly loan report containing loan disbursement and application statistics."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Monthly report fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/reports/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getMonthlyLoanReport() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Monthly loan report fetched successfully.")
                .data(loanService.getMonthlyLoanReport())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Loan Analytics",
            description = "Returns analytics for a selected loan type."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan analytics fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid loan type"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanAnalytics(

            @Parameter(
                    description = "Loan Type",
                    example = "HOME"
            )
            @RequestParam LoanType loanType) {

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("Loan analytics fetched successfully.")
                        .data(
                                loanService.getLoanAnalytics(
                                        loanType))
                        .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Generate EMI Schedule",
            description = "Returns complete EMI schedule for a loan."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EMI schedule fetched successfully"),

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
    @GetMapping("/{loanId}/emi-schedule")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<ApiResponse> getEmiSchedule(

            @Parameter(
                    description = "Loan ID",
                    example = "1"
            )
            @PathVariable Long loanId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("EMI schedule fetched successfully.")
                .data(loanService.generateEmiSchedule(loanId))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Loan Statement",
            description = "Returns detailed loan information including customer details, repayment summary and complete repayment history."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan statement fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Loan not found"
            )
    })
    @GetMapping("/{loanId}/statement")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanStatement(

            @Parameter(
                    description = "Loan ID",
                    required = true,
                    example = "3"
            )
            @PathVariable Long loanId) {

        log.info(
                "Fetching loan statement. loanId={}",
                loanId
        );

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("Loan statement fetched successfully.")
                        .data(
                                loanService.getLoanStatement(loanId)
                        )
                        .build();

        return ResponseEntity.ok(response);
    }



@Operation(
            summary = "Get Foreclosure Details",
            description = "Calculates foreclosure amount based on loan and paid installments."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Foreclosure details fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid paid installments"),

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
    @GetMapping("/{loanId}/foreclosure")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getForeclosureDetails(

            @Parameter(
                    description = "Loan ID",
                    example = "1"
            )
            @PathVariable Long loanId,

            @Parameter(
                    description = "Number of paid installments",
                    example = "12"
            )
            @RequestParam Integer paidInstallments) {

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("Foreclosure details fetched successfully.")
                        .data(
                                loanService.getForeclosureDetails(
                                        loanId,
                                        paidInstallments))
                        .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Disburse an approved loan",
            description = "Disburses an APPROVED loan, generates full EMI schedule and moves the loan to ACTIVE status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loan disbursed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Loan is not in APPROVED status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @PostMapping("/{loanId}/disburse")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> disburseLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody DisburseLoanRequestDto request) {

        LoanResponseDto responseDto = loanService.disburseLoan(loanId, request);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan disbursed successfully and EMI schedule generated.")
                .data(responseDto)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get EMI Schedule (from database)",
            description = "Returns the full EMI schedule stored in the database for a loan."
    )
    @GetMapping("/{loanId}/emi-schedule-db")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<ApiResponse> getEmiScheduleFromDb(@PathVariable Long loanId) {

        List<EmiScheduleResponseDto> schedule = loanService.getEmiScheduleFromDb(loanId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("EMI schedule fetched successfully.")
                .data(schedule)
                .build();

        return ResponseEntity.ok(response);
    }




}




