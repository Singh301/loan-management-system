package com.sudhanshu.loanmanagement.loan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, unique = true, length = 50)
    private String productCode;          // e.g. HOME_LOAN_STD

    @Column(nullable = false, length = 100)
    private String productName;          // e.g. Standard Home Loan

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer minTenureMonths;

    @Column(nullable = false)
    private Integer maxTenureMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal processingFeePercent;   // e.g. 1.00 = 1%

    @Column(precision = 10, scale = 2)
    private BigDecimal lateFeeAmount;          // Fixed late fee per missed EMI

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}