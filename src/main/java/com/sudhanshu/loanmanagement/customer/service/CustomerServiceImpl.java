package com.sudhanshu.loanmanagement.customer.service;

import com.sudhanshu.loanmanagement.customer.dto.*;
import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.exception.CustomerAlreadyExistsException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import com.sudhanshu.loanmanagement.customer.specification.CustomerSpecification;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final LoanRepository loanRepository;

    @Override
    public CustomerResponseDto addCustomer(CustomerRequestDto dto) {

        log.info("Received request to create customer with email: {}", dto.getEmail());

        if (customerRepository.existsByEmail(dto.getEmail())) {
            log.error("Customer already exists with email: {}", dto.getEmail());

            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email : " + dto.getEmail());
        }

        if (customerRepository.existsByMobileNumber(dto.getMobileNumber())) {
            log.error("Customer already exists with mobile number: {}",
                    dto.getMobileNumber());

            throw new CustomerAlreadyExistsException(
                    "Customer already exists with mobile number : "
                            + dto.getMobileNumber());
        }

        Customer customer = Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .panNumber(dto.getPanNumber())
                .aadhaarNumber(dto.getAadhaarNumber())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pinCode(dto.getPinCode())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        auditService.saveAudit(
                "SYSTEM",
                "CUSTOMER",
                "CREATE",
                "Customer created with email : " + savedCustomer.getEmail()
        );

        log.info(
                "Customer created successfully with email: {}",
                savedCustomer.getEmail()
        );

        return mapToResponse(savedCustomer);
    }

    @Override
    public List<CustomerResponseDto> getAllCustomers() {

        log.info("Fetching all customers");

        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CustomerResponseDto getCustomerById(Long customerId) {

        log.info("Fetching customer with id {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id : " + customerId));

        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDto updateCustomer(Long customerId,
                                              CustomerRequestDto dto) {

        log.info("Updating customer with id {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        // Email validation
        if (!customer.getEmail().equals(dto.getEmail())
                && customerRepository.existsByEmail(dto.getEmail())) {

            throw new CustomerAlreadyExistsException(
                    "Email already exists.");
        }

        // Mobile validation
        if (!customer.getMobileNumber().equals(dto.getMobileNumber())
                && customerRepository.existsByMobileNumber(dto.getMobileNumber())) {

            throw new CustomerAlreadyExistsException(
                    "Mobile number already exists.");
        }

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setMobileNumber(dto.getMobileNumber());
        customer.setPanNumber(dto.getPanNumber());
        customer.setAadhaarNumber(dto.getAadhaarNumber());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setState(dto.getState());
        customer.setPinCode(dto.getPinCode());

        Customer updatedCustomer = customerRepository.save(customer);

        auditService.saveAudit(
                "SYSTEM",
                "CUSTOMER",
                "UPDATE",
                "Customer updated : " + updatedCustomer.getCustomerId()
        );

        log.info("Customer updated successfully.");

        return mapToResponse(updatedCustomer);

    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id : " + customerId));

        customer.setDeleted(true);
        customer.setDeletedAt(java.time.LocalDateTime.now());
        customerRepository.save(customer);

        auditService.saveAudit("SYSTEM", "CUSTOMER", "SOFT_DELETE",
                "Customer soft deleted : " + customer.getCustomerId());
    }

    @Override
    public Page<CustomerResponseDto> searchCustomers(

            String name,
            String email,
            String mobile,
            String city,
            String state,
            int page,
            int size,
            String sortBy,
            String direction) {

        log.info("Searching customers...");

        Sort sort = direction.equalsIgnoreCase("desc")

                ? Sort.by(sortBy).descending()

                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return customerRepository.findAll(
                CustomerSpecification.search(
                        name,
                        email,
                        mobile,
                        city,
                        state
                ),
                pageable
        ).map(this::mapToResponse);
    }

    @Override
    public CustomerDashboardResponseDto getDashboard() {

        return CustomerDashboardResponseDto.builder()

                .totalCustomers(customerRepository.count())

                .customersWithLoans(
                        customerRepository.countCustomersWithLoans())

                .customersWithoutLoans(
                        customerRepository.countCustomersWithoutLoans())

                .totalLoans(loanRepository.count())

                .totalLoanAmount(
                        loanRepository.getTotalLoanAmount())

                .totalApprovedLoanAmount(
                        loanRepository.getTotalApprovedLoanAmount())

                .build();
    }

    private CustomerResponseDto mapToResponse(Customer customer) {

        return CustomerResponseDto.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .mobileNumber(customer.getMobileNumber())
                .panNumber(customer.getPanNumber())
                .aadhaarNumber(customer.getAadhaarNumber())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .pinCode(customer.getPinCode())
                .build();
    }

    @Override
    public CustomerSummaryResponseDto getCustomerSummary(Long customerId) {

        log.info("Fetching customer summary. customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        Loan latestLoan =
                loanRepository.findTopByCustomerCustomerIdOrderByApplicationDateDesc(customerId);

        long totalLoans =
                loanRepository.countByCustomerCustomerId(customerId);

        long pendingLoans =
                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                        customerId,
                        LoanStatus.PENDING);

        long approvedLoans =
                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                        customerId,
                        LoanStatus.APPROVED);

        long rejectedLoans =
                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                        customerId,
                        LoanStatus.REJECTED);

        BigDecimal totalLoanAmount =
                loanRepository.getCustomerTotalLoanAmount(customerId);

        if (totalLoanAmount == null) {
            totalLoanAmount = BigDecimal.ZERO;
        }

        BigDecimal approvedLoanAmount =
                loanRepository.getApprovedLoanAmount(customerId);

        if (approvedLoanAmount == null) {
            approvedLoanAmount = BigDecimal.ZERO;
        }

        CustomerSummaryResponseDto response =
                CustomerSummaryResponseDto.builder()
                        .customerId(customer.getCustomerId())
                        .customerName(
                                customer.getFirstName() + " " + customer.getLastName())
                        .email(customer.getEmail())
                        .mobileNumber(customer.getMobileNumber())
                        .totalLoans(totalLoans)
                        .pendingLoans(pendingLoans)
                        .approvedLoans(approvedLoans)
                        .rejectedLoans(rejectedLoans)
                        .totalLoanAmount(totalLoanAmount)
                        .approvedLoanAmount(approvedLoanAmount)
                        .latestLoanStatus(
                                latestLoan != null
                                        ? latestLoan.getLoanStatus()
                                        : null)
                        .latestLoanDate(
                                latestLoan != null
                                        ? latestLoan.getApplicationDate()
                                        : null)
                        .build();

        log.info(
                "Customer summary fetched successfully. customerId={}, totalLoans={}",
                customerId,
                totalLoans);

        return response;
    }

    @Override
    public CustomerResponseDto getMyProfile(String username) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateMyProfile(String username, CustomerUpdateSelfDto dto) {
        Customer customer = customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user: " + username));

        if (dto.getMobileNumber() != null && !dto.getMobileNumber().isBlank()) {
            customer.setMobileNumber(dto.getMobileNumber());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            customer.setCity(dto.getCity());
        }
        if (dto.getState() != null) {
            customer.setState(dto.getState());
        }
        if (dto.getPinCode() != null) {
            customer.setPinCode(dto.getPinCode());
        }

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }
}




