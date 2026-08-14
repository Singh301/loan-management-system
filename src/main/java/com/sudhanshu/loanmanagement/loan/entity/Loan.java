package com.sudhanshu.loanmanagement.loan.entity;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.repayment.entity.LoanRepayment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal emi;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanStatus loanStatus = LoanStatus.PENDING;

    @Builder.Default
    private LocalDate applicationDate = LocalDate.now();

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;

    @Builder.Default
    private Integer paidInstallments = 0;

    @Builder.Default
    private Integer remainingInstallments = 0;

    @OneToMany(
            mappedBy = "loan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<LoanRepayment> repayments;

    // Add these fields inside the Loan class

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private LoanProduct loanProduct;

    private LocalDate disbursementDate;

    private LocalDate nextDueDate;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalLateFee = BigDecimal.ZERO;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmiSchedule> emiSchedules = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private java.time.LocalDateTime deletedAt;

    /**
     * Optimistic locking version – prevents concurrent modification of the same loan.
     */
    @Version
    private Long version;

    /**
     * Idempotency key used during disbursement to prevent double disbursement.
     */
    @Column(name = "disbursement_idempotency_key", length = 100)
    private String disbursementIdempotencyKey;

}




