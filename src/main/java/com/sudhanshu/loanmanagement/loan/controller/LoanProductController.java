package com.sudhanshu.loanmanagement.loan.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.loan.dto.LoanProductRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanProductResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import com.sudhanshu.loanmanagement.loan.service.LoanProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/loan-products")
@RequiredArgsConstructor
@Tag(name = "Loan Product Management", description = "APIs for managing loan products")
@SecurityRequirement(name = "bearerAuth")
public class LoanProductController {

    private final LoanProductService loanProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new Loan Product")
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody LoanProductRequestDto request) {
        LoanProductResponseDto product = loanProductService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .success(true)
                        .message("Loan Product created successfully")
                        .data(product)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get all active Loan Products")
    public ResponseEntity<ApiResponse> getAllActiveProducts() {
        List<LoanProductResponseDto> products = loanProductService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Active loan products fetched successfully")
                .data(products)
                .build());
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get Loan Product by ID")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Loan Product fetched successfully")
                .data(loanProductService.getProductById(productId))
                .build());
    }

    @GetMapping("/type/{loanType}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Get Loan Products by Type")
    public ResponseEntity<ApiResponse> getProductsByType(@PathVariable LoanType loanType) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Loan products fetched successfully")
                .data(loanProductService.getProductsByType(loanType))
                .build());
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Loan Product")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody LoanProductRequestDto request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Loan Product updated successfully")
                .data(loanProductService.updateProduct(productId, request))
                .build());
    }

    @PatchMapping("/{productId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate Loan Product")
    public ResponseEntity<ApiResponse> deactivateProduct(@PathVariable Long productId) {
        loanProductService.deactivateProduct(productId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Loan Product deactivated successfully")
                .data(null)
                .build());
    }
}