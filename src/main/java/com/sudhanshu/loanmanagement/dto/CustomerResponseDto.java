package com.sudhanshu.loanmanagement.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {

    private Long customerId;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String panNumber;

    private String aadhaarNumber;

    private String address;

    private String city;

    private String state;

    private String pinCode;
}