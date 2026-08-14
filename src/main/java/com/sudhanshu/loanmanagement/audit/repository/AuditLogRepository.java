package com.sudhanshu.loanmanagement.audit.repository;

import com.sudhanshu.loanmanagement.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

}




