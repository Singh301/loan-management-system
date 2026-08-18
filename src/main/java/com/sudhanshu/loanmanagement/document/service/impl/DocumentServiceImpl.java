package com.sudhanshu.loanmanagement.document.service.impl;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.document.dto.DocumentResponseDto;
import com.sudhanshu.loanmanagement.document.dto.DocumentVerifyRequestDto;
import com.sudhanshu.loanmanagement.document.entity.Document;
import com.sudhanshu.loanmanagement.document.entity.DocumentType;
import com.sudhanshu.loanmanagement.document.entity.DocumentVerificationStatus;
import com.sudhanshu.loanmanagement.document.repository.DocumentRepository;
import com.sudhanshu.loanmanagement.document.service.DocumentService;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.security.OwnershipGuard;
import com.sudhanshu.loanmanagement.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;
    private final OwnershipGuard ownershipGuard;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            ".pdf", ".jpg", ".jpeg", ".png", ".webp"
    );

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only PDF and image files (JPEG, PNG, WEBP) are allowed.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Original file name is required.");
        }
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name.");
        }
        String lower = originalName.toLowerCase();
        if (ALLOWED_EXTENSIONS.stream().noneMatch(lower::endsWith)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: pdf, jpg, jpeg, png, webp.");
        }
    }

    private String extensionOf(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            return original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        return "";
    }

    @Override
    @Transactional
    public DocumentResponseDto uploadDocument(Long customerId, DocumentType documentType, MultipartFile file) {
        ownershipGuard.assertCanAccessCustomer(customerId);
        validateFile(file);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id : " + customerId));

        try {
            String storageKey = fileStorageService.store(file, extensionOf(file));

            Document document = Document.builder()
                    .customer(customer)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(storageKey)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Document saved = documentRepository.save(document);
            log.info("Document uploaded. documentId={}, customerId={}", saved.getDocumentId(), customerId);
            return mapToResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getCustomerDocuments(Long customerId) {
        ownershipGuard.assertCanAccessCustomer(customerId);
        return documentRepository.findByCustomerCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id : " + documentId));

        ownershipGuard.assertCanAccessCustomer(document.getCustomer().getCustomerId());

        try {
            return fileStorageService.load(document.getFilePath());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to download document.", ex);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id : " + documentId));

        ownershipGuard.assertCanAccessCustomer(document.getCustomer().getCustomerId());

        fileStorageService.delete(document.getFilePath());
        documentRepository.delete(document);
        log.info("Document deleted. documentId={}", documentId);
    }

    @Override
    @Transactional
    public void verifyDocument(Long documentId, DocumentVerifyRequestDto request, Long verifiedByUserId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        if (request.getStatus() == DocumentVerificationStatus.REJECTED
                && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is mandatory when rejecting");
        }

        doc.setVerificationStatus(request.getStatus());
        doc.setVerifiedBy(verifiedByUserId);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setRejectionReason(request.getRejectionReason());
        documentRepository.save(doc);
    }

    private DocumentResponseDto mapToResponse(Document document) {
        return DocumentResponseDto.builder()
                .documentId(document.getDocumentId())
                .customerId(document.getCustomer().getCustomerId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
