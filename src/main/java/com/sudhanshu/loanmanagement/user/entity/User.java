package com.sudhanshu.loanmanagement.user.entity;

import com.sudhanshu.loanmanagement.auth.entity.RefreshToken;
import com.sudhanshu.loanmanagement.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Customer customer;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private RefreshToken refreshToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    private java.time.LocalDateTime deletedAt;

}




