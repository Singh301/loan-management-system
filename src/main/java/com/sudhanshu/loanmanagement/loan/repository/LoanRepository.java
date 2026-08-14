package com.sudhanshu.loanmanagement.loan.repository;

import com.sudhanshu.loanmanagement.loan.dto.LoanMonthlyReportDto;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    List<Loan> findByCustomerCustomerId(Long customerId);

    @Query("SELECT l FROM Loan l JOIN FETCH l.customer WHERE l.loanId = :loanId")
    Optional<Loan> findByIdWithCustomer(@Param("loanId") Long loanId);

    /**
     * Serializes critical balance-changing operations for one loan. Optimistic locking remains
     * useful for ordinary updates; money movement uses a row lock to prevent lost updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.loanId = :loanId")
    Optional<Loan> findByIdForUpdate(@Param("loanId") Long loanId);

    Page<Loan> findByLoanStatus(LoanStatus loanStatus, Pageable pageable);
    Page<Loan> findByLoanType(LoanType loanType, Pageable pageable);
    long countByLoanStatus(LoanStatus loanStatus);

    @Query("SELECT COALESCE(SUM(l.loanAmount), 0) FROM Loan l WHERE l.loanStatus='APPROVED'")
    BigDecimal getTotalApprovedLoanAmount();

    @Query("SELECT COALESCE(SUM(l.loanAmount),0) FROM Loan l")
    BigDecimal getTotalLoanAmount();

    long countByCustomerCustomerId(Long customerId);
    long countByCustomerCustomerIdAndLoanStatus(Long customerId, LoanStatus loanStatus);

    @Query("SELECT COALESCE(SUM(l.loanAmount),0) FROM Loan l WHERE l.customer.customerId=:customerId")
    BigDecimal getCustomerTotalLoanAmount(Long customerId);

    @Query("SELECT COALESCE(SUM(l.loanAmount),0) FROM Loan l WHERE l.customer.customerId=:customerId AND l.loanStatus='APPROVED'")
    BigDecimal getApprovedLoanAmount(Long customerId);

    Loan findTopByCustomerCustomerIdOrderByApplicationDateDesc(Long customerId);

    @Query("SELECT COALESCE(AVG(l.loanAmount),0) FROM Loan l")
    BigDecimal getAverageLoanAmount();

    @Query("SELECT COALESCE(MAX(l.loanAmount),0) FROM Loan l")
    BigDecimal getHighestLoanAmount();

    @Query("SELECT COALESCE(MIN(l.loanAmount),0) FROM Loan l")
    BigDecimal getLowestLoanAmount();

    @Query("""
        SELECT new com.sudhanshu.loanmanagement.loan.dto.LoanMonthlyReportDto(
          YEAR(l.applicationDate), MONTH(l.applicationDate), COUNT(l), COALESCE(SUM(l.loanAmount),0))
        FROM Loan l
        GROUP BY YEAR(l.applicationDate), MONTH(l.applicationDate)
        ORDER BY YEAR(l.applicationDate), MONTH(l.applicationDate)
        """)
    List<LoanMonthlyReportDto> getMonthlyLoanReport();

    @Query("""
        SELECT COUNT(l) AS totalLoans,
               COALESCE(SUM(l.loanAmount), 0) AS totalAmount,
               COALESCE(AVG(l.loanAmount), 0) AS averageLoanAmount
        FROM Loan l WHERE l.loanType = :loanType
        """)
    LoanAnalyticsProjection getLoanAnalyticsByType(@Param("loanType") LoanType loanType);

    List<Loan> findByLoanType(LoanType loanType);
}
