package com.sudhanshu.loanmanagement.audit.service;
import com.sudhanshu.loanmanagement.audit.dto.AuditResponseDto;

import java.util.List;

public interface AuditService {

    void saveAudit(
            String username,
            String module,
            String action,
            String description);

    List<AuditResponseDto> getAllAudits();

}




