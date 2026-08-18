package com.sudhanshu.loanmanagement.loan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_approvals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoanApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    private Long approverUserId;

    private Integer level;          // 1 = Manager, 2 = Admin

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    private String remarks;

    private LocalDateTime actionAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}