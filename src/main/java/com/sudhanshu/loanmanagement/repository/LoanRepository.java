package com.sudhanshu.loanmanagement.repository;

import com.sudhanshu.loanmanagement.entity.Loan;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerCustomerId(Long customerId);

    Page<Loan> findByLoanStatus(
            LoanStatus loanStatus,
            Pageable pageable);

    Page<Loan> findByLoanType(
            LoanType loanType,
            Pageable pageable);

    long countByLoanStatus(LoanStatus loanStatus);

    @Query("""
            SELECT COALESCE(SUM(l.loanAmount), 0)
            FROM Loan l
            WHERE l.loanStatus='APPROVED'
            """)
    BigDecimal getTotalApprovedLoanAmount();

}