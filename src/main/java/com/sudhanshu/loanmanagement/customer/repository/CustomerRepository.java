package com.sudhanshu.loanmanagement.customer.repository;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends
        JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByPanNumber(String panNumber);

    Optional<Customer> findByUserUsername(String username);

    Optional<Customer> findByEmail(String email);

    @Query("""
            SELECT COUNT(DISTINCT c)
            FROM Customer c
            JOIN Loan l
                ON c.customerId = l.customer.customerId
            """)
    long countCustomersWithLoans();

    @Query("""
            SELECT COUNT(c)
            FROM Customer c
            WHERE c.customerId NOT IN
            (
                SELECT DISTINCT l.customer.customerId
                FROM Loan l
            )
            """)
    long countCustomersWithoutLoans();

}





