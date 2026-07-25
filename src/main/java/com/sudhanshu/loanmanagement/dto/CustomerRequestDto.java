package com.sudhanshu.loanmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequestDto {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String mobileNumber;

    @NotBlank
    private String panNumber;

    @NotBlank
    private String aadhaarNumber;

    private String address;
    private String city;
    private String state;
    private String pinCode;
}