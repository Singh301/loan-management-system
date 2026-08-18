package com.sudhanshu.loanmanagement.customer.specification;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;

public class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> search(
            String name,
            String email,
            String mobile,
            String city,
            String state) {

        return (root, query, cb) -> {

            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (name != null && !name.isBlank()) {

                String searchName = "%" +
                        name.trim().toLowerCase(Locale.ROOT) +
                        "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("firstName")),
                                        searchName
                                ),
                                cb.like(
                                        cb.lower(root.get("lastName")),
                                        searchName
                                )
                        )
                );
            }

            if (email != null && !email.isBlank()) {

                String searchEmail = "%" +
                        email.trim().toLowerCase(Locale.ROOT) +
                        "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("email")),
                                searchEmail
                        )
                );
            }

            if (mobile != null && !mobile.isBlank()) {

                String searchMobile = "%" +
                        mobile.trim() +
                        "%";

                predicates.add(
                        cb.like(
                                root.get("mobileNumber"),
                                searchMobile
                        )
                );
            }

            if (city != null && !city.isBlank()) {

                String searchCity = "%" +
                        city.trim().toLowerCase(Locale.ROOT) +
                        "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("city")),
                                searchCity
                        )
                );
            }

            if (state != null && !state.isBlank()) {

                String searchState = "%" +
                        state.trim().toLowerCase(Locale.ROOT) +
                        "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("state")),
                                searchState
                        )
                );
            }

            return cb.and(
                    predicates.toArray(
                            new jakarta.persistence.criteria.Predicate[0]
                    )
            );
        };
    }
}




