package com.sudhanshu.loanmanagement.dashboard.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sudhanshu.loanmanagement.constants.ApiConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/dashboard")
@RequiredArgsConstructor
@Tag(
        name = "Dashboard Management",
        description = "Dashboard APIs for loan statistics and business insights."
)
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get Loan Dashboard",
            description = "Returns dashboard statistics including loan counts, approvals, rejections, pending loans, total amount and other business metrics."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Loan dashboard fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping("/loan-summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getLoanDashboard() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Loan dashboard fetched successfully.")
                .data(dashboardService.getLoanDashboard())
                .build();

        return ResponseEntity.ok(response);
    }

}




