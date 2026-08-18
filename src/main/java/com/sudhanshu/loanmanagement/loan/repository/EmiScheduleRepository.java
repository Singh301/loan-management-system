package com.sudhanshu.loanmanagement.loan.repository;

import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanLoanIdOrderByInstallmentNumberAsc(Long loanId);

    List<EmiSchedule> findByLoanLoanIdAndStatus(Long loanId, EmiStatus status);

    @Query("SELECT e FROM EmiSchedule e WHERE e.status = 'PENDING' AND e.dueDate < :today")
    List<EmiSchedule> findOverdueEmis(@Param("today") LocalDate today);

    @Query("SELECT e FROM EmiSchedule e WHERE e.loan.loanId = :loanId AND e.status IN ('PENDING', 'OVERDUE') ORDER BY e.installmentNumber")
    List<EmiSchedule> findPendingOrOverdueByLoanId(@Param("loanId") Long loanId);
}