package com.sudhanshu.loanmanagement.controller;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.dto.CustomerRequestDto;
import com.sudhanshu.loanmanagement.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> addCustomer(
            @Valid @RequestBody CustomerRequestDto dto) {

        customerService.addCustomer(dto);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer created successfully.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<ApiResponse> getAllCustomers() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customers fetched successfully.")
                .data(customerService.getAllCustomers())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    public ResponseEntity<ApiResponse> getCustomerById(
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer fetched successfully.")
                .data(customerService.getCustomerById(customerId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerRequestDto dto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer updated successfully.")
                .data(customerService.updateCustomer(customerId, dto))
                .build();

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCustomer(
            @PathVariable Long customerId) {

        customerService.deleteCustomer(customerId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer deleted successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);

    }

}