package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.DocumentResponseDto;
import com.sudhanshu.loanmanagement.entity.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponseDto uploadDocument(
            Long customerId,
            DocumentType documentType,
            MultipartFile file);

    List<DocumentResponseDto> getCustomerDocuments(Long customerId);

    byte[] downloadDocument(Long documentId);

    void deleteDocument(Long documentId);

}