package com.sudhanshu.loanmanagement.loan.repository;

import com.sudhanshu.loanmanagement.loan.entity.LoanApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanApprovalRepository extends JpaRepository<LoanApproval, Long> {
    List<LoanApproval> findByLoanLoanIdOrderByLevelAsc(Long loanId);
}