package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.CustomerRequestDto;
import com.sudhanshu.loanmanagement.dto.CustomerResponseDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.exception.CustomerAlreadyExistsException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public void addCustomer(CustomerRequestDto dto) {

        log.info("Received request to create customer with email: {}", dto.getEmail());

        if (customerRepository.existsByEmail(dto.getEmail())) {

            log.error("Customer already exists with email: {}", dto.getEmail());

            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email : " + dto.getEmail());
        }

        if (customerRepository.existsByMobileNumber(dto.getMobileNumber())) {

            log.error("Customer already exists with mobile number: {}", dto.getMobileNumber());

            throw new CustomerAlreadyExistsException(
                    "Customer already exists with mobile number : " + dto.getMobileNumber());
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

        customerRepository.save(customer);

        log.info("Customer created successfully with email: {}", dto.getEmail());
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

        log.info("Customer updated successfully.");

        return mapToResponse(updatedCustomer);

    }

    @Override
    public void deleteCustomer(Long customerId) {

        log.info("Deleting customer {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        customerRepository.delete(customer);

        log.info("Customer deleted successfully.");

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
}