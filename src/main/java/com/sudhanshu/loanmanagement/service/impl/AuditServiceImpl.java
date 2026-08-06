package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.AuditResponseDto;
import com.sudhanshu.loanmanagement.entity.AuditLog;
import com.sudhanshu.loanmanagement.repository.AuditLogRepository;
import com.sudhanshu.loanmanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void saveAudit(
            String username,
            String module,
            String action,
            String description) {

        AuditLog audit = AuditLog.builder()
                .username(username)
                .module(module)
                .action(action)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(audit);
    }

    @Override
    public List<AuditResponseDto> getAllAudits() {

        return auditLogRepository.findAll()
                .stream()
                .map(audit -> AuditResponseDto.builder()
                        .auditId(audit.getAuditId())
                        .username(audit.getUsername())
                        .module(audit.getModule())
                        .action(audit.getAction())
                        .description(audit.getDescription())
                        .createdAt(audit.getCreatedAt())
                        .build())
                .toList();

    }
}