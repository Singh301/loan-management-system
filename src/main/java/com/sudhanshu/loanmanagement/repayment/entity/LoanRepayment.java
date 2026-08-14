package com.sudhanshu.loanmanagement.repayment.entity;

import com.sudhanshu.loanmanagement.loan.entity.Loan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_repayments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal principalPaid;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal interestPaid;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal remainingPrincipal;

    private LocalDateTime paymentDate;

    @Column(length = 30)
    private String paymentMode;

    @Column(length = 100, unique = true)
    private String transactionReference;

    @Column(length = 500)
    private String remarks;

    /**
     * Optimistic locking version.
     */
    @Version
    private Long version;

}




