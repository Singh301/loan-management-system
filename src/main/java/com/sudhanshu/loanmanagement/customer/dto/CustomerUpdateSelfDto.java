package com.sudhanshu.loanmanagement.customer.dto;

import lombok.Data;

@Data
public class CustomerUpdateSelfDto {
    private String mobileNumber;
    private String address;
    private String city;
    private String state;
    private String pinCode;
}