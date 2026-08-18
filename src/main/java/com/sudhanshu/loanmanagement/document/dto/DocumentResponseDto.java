package com.sudhanshu.loanmanagement.document.dto;

import com.sudhanshu.loanmanagement.document.entity.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(
        name = "Document Response",
        description = "Represents a customer document uploaded into the Loan Management System."
)
public class DocumentResponseDto {

    @Schema(
            description = "Unique document identifier",
            example = "501"
    )
    private Long documentId;

    @Schema(
            description = "Customer identifier associated with the document",
            example = "101"
    )
    private Long customerId;

    @Schema(
            description = "Type of uploaded document",
            example = "AADHAAR",
            allowableValues = {
                    "AADHAAR",
                    "PAN",
                    "PASSPORT",
                    "DRIVING_LICENSE",
                    "BANK_STATEMENT",
                    "SALARY_SLIP",
                    "INCOME_PROOF",
                    "ADDRESS_PROOF",
                    "PHOTO",
                    "OTHER"
            }
    )
    private DocumentType documentType;

    @Schema(
            description = "Original uploaded file name",
            example = "aadhaar_card.pdf"
    )
    private String fileName;

    @Schema(
            description = "Uploaded file MIME type",
            example = "application/pdf"
    )
    private String fileType;

    @Schema(
            description = "Uploaded file size in bytes",
            example = "254896"
    )
    private Long fileSize;

    @Schema(
            description = "Date and time when the document was uploaded",
            example = "2026-08-07T16:45:12"
    )
    private LocalDateTime uploadedAt;

}




