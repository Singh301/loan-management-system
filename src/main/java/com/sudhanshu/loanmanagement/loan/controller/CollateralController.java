package com.sudhanshu.loanmanagement.loan.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.loan.dto.CollateralRequestDto;
import com.sudhanshu.loanmanagement.loan.service.CollateralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/loans")
@RequiredArgsConstructor
@Tag(name = "Collateral Management")
@SecurityRequirement(name = "bearerAuth")
public class CollateralController {

    private final CollateralService collateralService;

    @PostMapping("/{loanId}/collaterals")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Add collateral to a loan")
    public ResponseEntity<ApiResponse> addCollateral(
            @PathVariable Long loanId,
            @Valid @RequestBody CollateralRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .success(true)
                        .message("Collateral added successfully")
                        .data(collateralService.addCollateral(loanId, request))
                        .build());
    }

    @GetMapping("/{loanId}/collaterals")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get collaterals of a loan")
    public ResponseEntity<ApiResponse> getCollaterals(@PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Collaterals fetched successfully")
                .data(collateralService.getCollateralsByLoan(loanId))
                .build());
    }

    @DeleteMapping("/collaterals/{collateralId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete collateral")
    public ResponseEntity<ApiResponse> deleteCollateral(@PathVariable Long collateralId) {
        collateralService.deleteCollateral(collateralId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Collateral deleted successfully")
                .data(null)
                .build());
    }
}