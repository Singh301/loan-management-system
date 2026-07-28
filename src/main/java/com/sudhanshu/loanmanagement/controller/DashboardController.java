package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

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