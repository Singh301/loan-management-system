package com.sudhanshu.loanmanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
        name = "Customer Request",
        description = "Request payload used to create or update customer information."
)
public class CustomerRequestDto {

    @Schema(
            description = "Customer's first name",
            example = "Sudhanshu"
    )
    @NotBlank(message = "First name is required")
    private String firstName;

    @Schema(
            description = "Customer's last name",
            example = "Singh"
    )
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Schema(
            description = "Customer email address",
            example = "sudhanshu@gmail.com"
    )
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(
            description = "10-digit mobile number",
            example = "9876543210"
    )
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @Schema(
            description = "PAN number of the customer",
            example = "ABCDE1234F"
    )
    @NotBlank(message = "PAN number is required")
    private String panNumber;

    @Schema(
            description = "12-digit Aadhaar number",
            example = "123412341234"
    )
    @NotBlank(message = "Aadhaar number is required")
    private String aadhaarNumber;

    @Schema(
            description = "Customer's residential address",
            example = "123 MG Road"
    )
    private String address;

    @Schema(
            description = "City of residence",
            example = "Jamshedpur"
    )
    private String city;

    @Schema(
            description = "State of residence",
            example = "Jharkhand"
    )
    private String state;

    @Schema(
            description = "Postal PIN code",
            example = "831001"
    )
    private String pinCode;

}




