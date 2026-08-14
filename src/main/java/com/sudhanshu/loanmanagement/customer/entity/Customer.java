package com.sudhanshu.loanmanagement.customer.entity;

import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String mobileNumber;

    @Column(nullable = false)
    private String panNumber;

    @Column(nullable = false)
    private String aadhaarNumber;

    private String address;

    private String city;

    private String state;

    private String pinCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            unique = true
    )
    @JsonIgnore
    private User user;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private java.time.LocalDateTime deletedAt;

    /**
     * Optimistic locking version.
     */
    @Version
    private Long version;
}




