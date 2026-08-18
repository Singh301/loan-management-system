package com.sudhanshu.loanmanagement.customer.controller;

import com.sudhanshu.loanmanagement.customer.dto.CustomerUpdateSelfDto;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.customer.dto.CustomerRequestDto;
import com.sudhanshu.loanmanagement.customer.dto.CustomerResponseDto;
import com.sudhanshu.loanmanagement.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.sudhanshu.loanmanagement.constants.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/customers")
@RequiredArgsConstructor

@Tag(
        name = "Customer Management",
        description = "Customer Registration, Search, Dashboard and Customer Management APIs"
)

@SecurityRequirement(name = "bearerAuth")

public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(
            summary = "Create Customer",
            description = "Creates a new customer. Accessible only by ADMIN and MANAGER."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Customer already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> addCustomer(
            @Valid
            @RequestBody
            CustomerRequestDto dto) {

        CustomerResponseDto customer =
                customerService.addCustomer(dto);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer created successfully.")
                .data(customer)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CUSTOMER')")
    @Operation(
            summary = "Get All Customers",
            description = "Returns the list of all customers. Accessible by ADMIN, MANAGER and CUSTOMER roles."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customers fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
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
    @Operation(
            summary = "Get Customer By ID",
            description = "Fetches customer details using the customer ID."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getCustomerById(

            @Parameter(
                    description = "Unique Customer ID",
                    required = true,
                    example = "1"
            )
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
    @Operation(
            summary = "Update Customer",
            description = "Updates an existing customer using the customer ID."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> updateCustomer(

            @Parameter(
                    description = "Customer ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long customerId,

            @Valid
            @RequestBody CustomerRequestDto dto) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer updated successfully.")
                .data(customerService.updateCustomer(customerId, dto))
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Customer",
            description = "Deletes a customer by customer ID. Accessible only by ADMIN."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer deleted successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> deleteCustomer(

            @Parameter(
                    description = "Customer ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long customerId) {

        customerService.deleteCustomer(customerId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer deleted successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(
            summary = "Get Customer Summary",
            description = "Returns customer profile along with loan summary."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer summary fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getCustomerSummary(

            @Parameter(
                    description = "Customer ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Customer summary fetched successfully.")
                .data(customerService.getCustomerSummary(customerId))
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(
            summary = "Search Customers",
            description = "Search customers using name, email, mobile, city, state and pagination."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customers fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> searchCustomers(

            @Parameter(description = "Customer Name", example = "Sudhanshu")
            @RequestParam(required = false) String name,

            @Parameter(description = "Email Address", example = "user@gmail.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Mobile Number", example = "9876543210")
            @RequestParam(required = false) String mobile,

            @Parameter(description = "City", example = "Delhi")
            @RequestParam(required = false) String city,

            @Parameter(description = "State", example = "Delhi")
            @RequestParam(required = false) String state,

            @Parameter(description = "Page Number", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page Size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort Field", example = "customerId")
            @RequestParam(defaultValue = "customerId") String sortBy,

            @Parameter(description = "Sort Direction", example = "asc")
            @RequestParam(defaultValue = "asc") String direction

    ) {

        return ResponseEntity.ok(

                ApiResponse.builder()

                        .success(true)

                        .message("Customers fetched successfully.")

                        .data(
                                customerService.searchCustomers(
                                        name,
                                        email,
                                        mobile,
                                        city,
                                        state,
                                        page,
                                        size,
                                        sortBy,
                                        direction
                                )
                        )

                        .build()
        );
    }


    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(
            summary = "Customer Dashboard",
            description = "Returns customer dashboard statistics."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer dashboard fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getDashboard() {

        ApiResponse response = ApiResponse.builder()

                .success(true)

                .message("Customer dashboard fetched successfully.")

                .data(customerService.getDashboard())

                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Profile fetched")
                .data(customerService.getMyProfile(username))
                .build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse> updateMyProfile(
            @Valid @RequestBody CustomerUpdateSelfDto dto,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Profile updated")
                .data(customerService.updateMyProfile(username, dto))
                .build());
    }

}




