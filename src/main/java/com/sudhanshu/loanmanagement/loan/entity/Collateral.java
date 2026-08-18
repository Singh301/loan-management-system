package com.sudhanshu.loanmanagement.loan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collaterals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collateralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, length = 50)
    private String collateralType;

    private String description;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    private String ownershipProof;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}