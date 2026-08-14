package com.sudhanshu.loanmanagement.config;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.user.entity.User;
import com.sudhanshu.loanmanagement.user.entity.Role;
import com.sudhanshu.loanmanagement.auth.entity.RefreshToken;
import com.sudhanshu.loanmanagement.document.entity.Document;
import com.sudhanshu.loanmanagement.document.entity.DocumentType;
import com.sudhanshu.loanmanagement.audit.entity.AuditLog;
import com.sudhanshu.loanmanagement.repayment.entity.LoanRepayment;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.user.repository.UserRepository;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmiCalculator emiCalculator;

    @Override
    public void run(String... args) {

        // 1. Create Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .username("admin")
                    .email("admin@loanmanagement.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created → admin / Admin@123");
        }

        // 2. Create Manager
        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = User.builder()
                    .fullName("Loan Manager")
                    .username("manager")
                    .email("manager@loanmanagement.com")
                    .password(passwordEncoder.encode("Manager@123"))
                    .role(Role.ROLE_MANAGER)
                    .enabled(true)
                    .build();
            userRepository.save(manager);
            log.info("Manager user created → manager / Manager@123");
        }

        // 3. Create sample Customers + Loans only if no customers exist
        if (customerRepository.count() == 0) {

            // Customer 1
            User user1 = User.builder()
                    .fullName("Rahul Sharma")
                    .username("rahul")
                    .email("rahul@example.com")
                    .password(passwordEncoder.encode("Rahul@123"))
                    .role(Role.ROLE_CUSTOMER)
                    .enabled(true)
                    .build();
            userRepository.save(user1);

            Customer customer1 = Customer.builder()
                    .firstName("Rahul")
                    .lastName("Sharma")
                    .email("rahul@example.com")
                    .mobileNumber("9876543210")
                    .panNumber("ABCDE1234F")
                    .aadhaarNumber("123456789012")
                    .address("12 MG Road")
                    .city("Bangalore")
                    .state("Karnataka")
                    .pinCode("560001")
                    .active(true)
                    .user(user1)
                    .build();
            customerRepository.save(customer1);

            // Customer 2
            User user2 = User.builder()
                    .fullName("Priya Patel")
                    .username("priya")
                    .email("priya@example.com")
                    .password(passwordEncoder.encode("Priya@123"))
                    .role(Role.ROLE_CUSTOMER)
                    .enabled(true)
                    .build();
            userRepository.save(user2);

            Customer customer2 = Customer.builder()
                    .firstName("Priya")
                    .lastName("Patel")
                    .email("priya@example.com")
                    .mobileNumber("9123456780")
                    .panNumber("FGHIJ5678K")
                    .aadhaarNumber("987654321098")
                    .address("45 Park Street")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .pinCode("400001")
                    .active(true)
                    .user(user2)
                    .build();
            customerRepository.save(customer2);

            // Sample Loans
            createSampleLoan(customer1, LoanType.HOME, new BigDecimal("2500000"), new BigDecimal("8.5"), 240);
            createSampleLoan(customer1, LoanType.CAR, new BigDecimal("800000"), new BigDecimal("9.2"), 60);
            createSampleLoan(customer2, LoanType.PERSONAL, new BigDecimal("300000"), new BigDecimal("12.5"), 36);
            createSampleLoan(customer2, LoanType.HOME, new BigDecimal("4500000"), new BigDecimal("8.75"), 300);

            log.info("Sample customers and loans created successfully");
        }
    }

    private void createSampleLoan(Customer customer, LoanType type,
                                  BigDecimal amount, BigDecimal rate, int tenure) {

        BigDecimal emi = emiCalculator.calculateEmi(amount, rate, tenure);

        Loan loan = Loan.builder()
                .customer(customer)
                .loanType(type)
                .loanAmount(amount)
                .interestRate(rate)
                .tenureMonths(tenure)
                .emi(emi)
                .loanStatus(LoanStatus.PENDING)
                .applicationDate(LocalDate.now().minusDays(5))
                .outstandingPrincipal(amount)
                .paidInstallments(0)
                .remainingInstallments(tenure)
                .remarks("Sample loan for testing")
                .build();

        loanRepository.save(loan);
    }
}




