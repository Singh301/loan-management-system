package com.sudhanshu.loanmanagement.service;
import com.sudhanshu.loanmanagement.dto.AuditResponseDto;

import java.util.List;

public interface AuditService {

    void saveAudit(
            String username,
            String module,
            String action,
            String description);

    List<AuditResponseDto> getAllAudits();

}