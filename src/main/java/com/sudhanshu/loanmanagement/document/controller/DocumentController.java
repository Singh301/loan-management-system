package com.sudhanshu.loanmanagement.document.controller;

import com.sudhanshu.loanmanagement.document.dto.DocumentVerifyRequestDto;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.document.entity.DocumentType;
import com.sudhanshu.loanmanagement.document.service.DocumentService;
import com.sudhanshu.loanmanagement.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sudhanshu.loanmanagement.constants.ApiConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/documents")
@RequiredArgsConstructor
@Tag(
        name = "Document Management",
        description = "APIs for uploading, downloading, retrieving and deleting customer documents."
)
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Upload customer document",
            description = "Uploads a document for the specified customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document uploaded successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized")
    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> uploadDocument(

            @Parameter(description = "Customer ID", example = "1")
            @RequestParam Long customerId,

            @Parameter(description = "Document Type", example = "AADHAR")
            @RequestParam DocumentType documentType,

            @Parameter(description = "Document File")
            @RequestParam MultipartFile file) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Document uploaded successfully.")
                .data(documentService.uploadDocument(
                        customerId,
                        documentType,
                        file))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get customer documents",
            description = "Returns all uploaded documents of a customer."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Documents fetched successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found")
    })
    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse> getCustomerDocuments(

            @Parameter(description = "Customer ID", example = "1")
            @PathVariable Long customerId) {

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Documents fetched successfully.")
                .data(documentService.getCustomerDocuments(customerId))
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Download document",
            description = "Downloads a customer document."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document downloaded successfully",
                    content = @Content),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document not found")
    })
    @GetMapping("/download/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ByteArrayResource> downloadDocument(

            @Parameter(description = "Document ID", example = "5")
            @PathVariable Long documentId) {

        byte[] data =
                documentService.downloadDocument(documentId);

        ByteArrayResource resource =
                new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(
            summary = "Delete document",
            description = "Deletes an uploaded customer document."
    )
    @ApiResponses(value = {

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Document deleted successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Document not found"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied")
    })
    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteDocument(

            @Parameter(description = "Document ID", example = "5")
            @PathVariable Long documentId) {

        documentService.deleteDocument(documentId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Document deleted successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{documentId}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Verify or Reject document")
    public ResponseEntity<ApiResponse> verifyDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentVerifyRequestDto request,
            Authentication authentication) {

        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId();
        documentService.verifyDocument(documentId, request, userId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Document verification updated")
                .data(null)
                .build());
    }
}




