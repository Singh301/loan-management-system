package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.CustomerRequestDto;
import com.sudhanshu.loanmanagement.dto.CustomerResponseDto;

import java.util.List;

public interface CustomerService {

    void addCustomer(CustomerRequestDto customerRequestDto);
    List<CustomerResponseDto> getAllCustomers();
    CustomerResponseDto getCustomerById(Long customerId);
    CustomerResponseDto updateCustomer(Long customerId,
                                       CustomerRequestDto dto);
    void deleteCustomer(Long customerId);
}