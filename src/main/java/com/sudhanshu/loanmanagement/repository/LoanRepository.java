package com.sudhanshu.loanmanagement.repository;

import com.sudhanshu.loanmanagement.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerCustomerId(Long customerId);
}