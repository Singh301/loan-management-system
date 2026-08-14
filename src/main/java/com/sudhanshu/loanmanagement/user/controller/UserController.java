package com.sudhanshu.loanmanagement.user.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.user.dto.UpdateUserRequestDto;
import com.sudhanshu.loanmanagement.user.dto.UpdateUserRoleRequestDto;
import com.sudhanshu.loanmanagement.user.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/users")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "User Management",
        description = "APIs for User Registration, User Management, Role Management and User Administration"
)
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in the Loan Management System.",
            security = {}
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> registerUser(

            @Valid
            @RequestBody
            UserRequestDto dto) {

        log.info("User registration request received.");

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User registered successfully.")
                        .data(userService.registerUser(dto))
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Current Logged-in User",
            description = "Returns the profile details of the currently authenticated user."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Current user fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getCurrentUser() {

        log.info("Fetching current logged-in user.");

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("Current user fetched successfully.")
                        .data(userService.getCurrentUser())
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get User by ID",
            description = "Fetches a user's complete details using the unique user ID. Only ADMIN users are authorized to access this endpoint."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID supplied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getUserById(

            @Parameter(
                    description = "Unique User ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId) {

        log.info("Fetching user by ID: {}", userId);

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User fetched successfully.")
                        .data(userService.getUserById(userId))
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get All Users",
            description = "Retrieves all users with pagination and sorting support. Accessible only by ADMIN users."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users fetched successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> getAllUsers(

            @Parameter(
                    description = "Page number (0-based)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of records per page",
                    example = "10"
            )
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(
                    description = "Field used for sorting",
                    example = "userId"
            )
            @RequestParam(defaultValue = "userId")
            String sortBy,

            @Parameter(
                    description = "Sorting direction (asc or desc)",
                    example = "asc"
            )
            @RequestParam(defaultValue = "asc")
            String direction) {

        log.info(
                "Fetching users. page={}, size={}, sortBy={}, direction={}",
                page,
                size,
                sortBy,
                direction);

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("Users fetched successfully.")
                        .data(
                                userService.getAllUsers(
                                        page,
                                        size,
                                        sortBy,
                                        direction))
                        .build();

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update User",
            description = "Updates the details of an existing user. Only ADMIN users are authorized to perform this operation."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid request",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> updateUser(

            @Parameter(
                    description = "Unique User ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId,

            @Valid
            @RequestBody
            UpdateUserRequestDto dto) {

        log.info("Updating user {}", userId);

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User updated successfully.")
                        .data(userService.updateUser(userId, dto))
                        .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Enable or Disable User",
            description = "Enables or disables a user account. Only ADMIN users are authorized to perform this operation."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User status updated successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> updateUserStatus(

            @Parameter(
                    description = "Unique User ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId,

            @Parameter(
                    description = "User account status (true = enabled, false = disabled)",
                    required = true,
                    example = "true"
            )
            @RequestParam Boolean enabled) {

        log.info(
                "Updating user status. userId={}, enabled={}",
                userId,
                enabled
        );

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User status updated successfully.")
                        .data(userService.enableDisableUser(userId, enabled))
                        .build();

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Change User Role",
            description = "Updates the role of an existing user. Only ADMIN users are authorized to perform this operation."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User role updated successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid role",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> changeRole(

            @Parameter(
                    description = "Unique User ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId,

            @Valid
            @RequestBody
            UpdateUserRoleRequestDto dto) {

        log.info("Changing role for user {}", userId);

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User role updated successfully.")
                        .data(userService.changeRole(userId, dto))
                        .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete User (Soft Delete)",
            description = "Soft deletes (disables) a user account. Only ADMIN users are authorized to perform this operation."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully"
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "User already disabled or invalid request",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token is missing or invalid",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content
            ),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse> deleteUser(

            @Parameter(
                    description = "Unique User ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long userId) {

        log.info("Deleting user {}", userId);

        userService.deleteUser(userId);

        ApiResponse response =
                ApiResponse.builder()
                        .success(true)
                        .message("User deleted successfully.")
                        .data(null)
                        .build();

        return ResponseEntity.ok(response);
    }
}




