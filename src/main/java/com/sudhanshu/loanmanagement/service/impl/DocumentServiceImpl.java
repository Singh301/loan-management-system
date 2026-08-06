package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.DocumentResponseDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.entity.Document;
import com.sudhanshu.loanmanagement.entity.DocumentType;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.repository.DocumentRepository;
import com.sudhanshu.loanmanagement.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;

    private void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("application/pdf")
                        && !contentType.startsWith("image/"))) {

            throw new IllegalArgumentException(
                    "Only PDF and image files are allowed.");
        }
    }

    private String saveFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName =
                UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    @Override
    public DocumentResponseDto uploadDocument(
            Long customerId,
            DocumentType documentType,
            MultipartFile file) {

        log.info("Uploading {} for customer {}",
                documentType,
                customerId);

        validateFile(file);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        try {

            String filePath = saveFile(file);

            Document document = Document.builder()
                    .customer(customer)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath(filePath)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            Document savedDocument =
                    documentRepository.save(document);

            log.info("Document uploaded successfully.");

            return mapToResponse(savedDocument);

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Unable to upload document.", ex);
        }

    }

    @Override
    public List<DocumentResponseDto> getCustomerDocuments(Long customerId) {

        log.info("Fetching documents for customer {}", customerId);

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        return documentRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }


    @Override
    public byte[] downloadDocument(Long documentId) {

        log.info("Downloading document {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found with id : " + documentId));

        try {

            return Files.readAllBytes(Paths.get(document.getFilePath()));

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Unable to download document.", ex);
        }

    }

    @Override
    public void deleteDocument(Long documentId) {

        log.info("Deleting document {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found with id : " + documentId));

        try {

            Files.deleteIfExists(Paths.get(document.getFilePath()));

        } catch (IOException ex) {

            log.warn("Unable to delete physical file.");
        }

        documentRepository.delete(document);

        log.info("Document deleted successfully.");

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