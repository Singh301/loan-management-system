package com.sudhanshu.loanmanagement.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "Customer Response",
        description = "Customer details returned by Customer Management APIs."
)
public class CustomerResponseDto {

    @Schema(
            description = "Unique customer identifier",
            example = "1"
    )
    private Long customerId;

    @Schema(
            description = "Customer's first name",
            example = "Sudhanshu"
    )
    private String firstName;

    @Schema(
            description = "Customer's last name",
            example = "Singh"
    )
    private String lastName;

    @Schema(
            description = "Registered email address",
            example = "sudhanshu@gmail.com"
    )
    private String email;

    @Schema(
            description = "10-digit mobile number",
            example = "9876543210"
    )
    private String mobileNumber;

    @Schema(
            description = "Permanent Account Number (PAN)",
            example = "ABCDE1234F"
    )
    private String panNumber;

    @Schema(
            description = "12-digit Aadhaar number",
            example = "123412341234"
    )
    private String aadhaarNumber;

    @Schema(
            description = "Residential address",
            example = "123 MG Road"
    )
    private String address;

    @Schema(
            description = "City",
            example = "Jamshedpur"
    )
    private String city;

    @Schema(
            description = "State",
            example = "Jharkhand"
    )
    private String state;

    @Schema(
            description = "Postal PIN code",
            example = "831001"
    )
    private String pinCode;
}




