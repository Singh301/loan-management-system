package com.sudhanshu.loanmanagement.audit.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.audit.service.AuditService;
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
@RequestMapping(ApiConstants.API_V1 + "/audits")
@RequiredArgsConstructor
@Tag(
        name = "Audit Management",
        description = "APIs for viewing system audit logs."
)
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;

    @Operation(
            summary = "Get All Audit Logs",
            description = "Returns all audit logs recorded in the system. Accessible only by ADMIN users."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit logs fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllAudits() {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Audit logs fetched successfully.")
                .data(auditService.getAllAudits())
                .build();

        return ResponseEntity.ok(response);
    }
}




