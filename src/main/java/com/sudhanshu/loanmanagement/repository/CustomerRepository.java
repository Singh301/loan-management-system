package com.sudhanshu.loanmanagement.repository;

import com.sudhanshu.loanmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByPanNumber(String panNumber);
}