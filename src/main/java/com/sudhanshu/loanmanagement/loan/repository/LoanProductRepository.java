package com.sudhanshu.loanmanagement.loan.repository;

import com.sudhanshu.loanmanagement.loan.entity.LoanProduct;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    Optional<LoanProduct> findByProductCode(String productCode);

    List<LoanProduct> findByActiveTrue();

    List<LoanProduct> findByLoanTypeAndActiveTrue(LoanType loanType);
}