package com.sudhanshu.loanmanagement.repayment.repository;

import com.sudhanshu.loanmanagement.repayment.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepaymentRepository
        extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanLoanIdOrderByPaymentDateDesc(
            Long loanId);

    boolean existsByTransactionReference(
            String transactionReference);

    @Query("""
            SELECT COALESCE(SUM(r.amountPaid),0)
            FROM LoanRepayment r
            WHERE r.loan.loanId=:loanId
            """)
    BigDecimal sumAmountPaidByLoanLoanId(Long loanId);

    @Query("""
            SELECT COALESCE(SUM(r.principalPaid),0)
            FROM LoanRepayment r
            WHERE r.loan.loanId=:loanId
            """)
    BigDecimal sumPrincipalPaidByLoanLoanId(Long loanId);

    @Query("""
            SELECT COALESCE(SUM(r.interestPaid),0)
            FROM LoanRepayment r
            WHERE r.loan.loanId=:loanId
            """)
    BigDecimal sumInterestPaidByLoanLoanId(Long loanId);
}




