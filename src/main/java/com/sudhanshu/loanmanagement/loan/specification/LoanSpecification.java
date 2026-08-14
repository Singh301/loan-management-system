package com.sudhanshu.loanmanagement.loan.specification;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class LoanSpecification {

    private LoanSpecification() {
    }

    public static Specification<Loan> search(
            String customerName,
            String email,
            LoanType loanType,
            LoanStatus loanStatus,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {

        return (root, query, cb) -> {

            Join<Loan, Customer> customer =
                    root.join("customer");

            List<Predicate> predicates =
                    new ArrayList<>();

            // Customer name search
            if (customerName != null
                    && !customerName.isBlank()) {

                String searchName =
                        "%" + customerName.trim().toLowerCase() + "%";

                Predicate firstNamePredicate =
                        cb.like(
                                cb.lower(
                                        customer.get("firstName")
                                ),
                                searchName
                        );

                Predicate lastNamePredicate =
                        cb.like(
                                cb.lower(
                                        customer.get("lastName")
                                ),
                                searchName
                        );

                predicates.add(
                        cb.or(
                                firstNamePredicate,
                                lastNamePredicate
                        )
                );
            }

            // Customer email search
            if (email != null
                    && !email.isBlank()) {

                String searchEmail =
                        "%" + email.trim().toLowerCase() + "%";

                predicates.add(
                        cb.like(
                                cb.lower(
                                        customer.get("email")
                                ),
                                searchEmail
                        )
                );
            }

            // Loan type
            if (loanType != null) {

                predicates.add(
                        cb.equal(
                                root.get("loanType"),
                                loanType
                        )
                );
            }

            // Loan status
            if (loanStatus != null) {

                predicates.add(
                        cb.equal(
                                root.get("loanStatus"),
                                loanStatus
                        )
                );
            }

            // Minimum loan amount
            if (minAmount != null) {

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("loanAmount"),
                                minAmount
                        )
                );
            }

            // Maximum loan amount
            if (maxAmount != null) {

                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("loanAmount"),
                                maxAmount
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}




