package com.sudhanshu.loanmanagement.customer.service;

import com.sudhanshu.loanmanagement.customer.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerService {

    CustomerResponseDto addCustomer(CustomerRequestDto dto);
    List<CustomerResponseDto> getAllCustomers();
    CustomerResponseDto getCustomerById(Long customerId);
    CustomerResponseDto updateCustomer(Long customerId,
                                       CustomerRequestDto dto);
    void deleteCustomer(Long customerId);
    Page<CustomerResponseDto> searchCustomers(

            String name,
            String email,
            String mobile,
            String city,
            String state,
            int page,
            int size,
            String sortBy,
            String direction
    );

    CustomerDashboardResponseDto getDashboard();

    CustomerSummaryResponseDto getCustomerSummary(Long customerId);

    CustomerResponseDto getMyProfile(String username);

    CustomerResponseDto updateMyProfile(String username, CustomerUpdateSelfDto dto);
}




