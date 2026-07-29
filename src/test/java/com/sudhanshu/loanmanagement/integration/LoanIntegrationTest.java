package com.sudhanshu.loanmanagement.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudhanshu.loanmanagement.dto.LoginRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Login and obtain JWT token
     */
    private String obtainJwtToken() throws Exception {

        LoginRequestDto login = new LoginRequestDto();
        login.setUsername("sudhanshu");
        login.setPassword("Admin@123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);

        return jsonNode.get("data")
                .get("token")
                .asText();
    }

    // ===========================================================
    // TEST CASE 1
    // Access without JWT
    // ===========================================================

    @Test
    void getAllLoans_ShouldReturnForbidden_WhenTokenMissing()
            throws Exception {

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isForbidden());
    }



    // ===========================================================
    // TEST CASE 3
    // Get All Loans
    // ===========================================================

    @Test
    void getAllLoans_ShouldReturnSuccess()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans")
                        .header("Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data")
                        .isArray());
    }

    // ===========================================================
    // TEST CASE 4
    // Get Loan By Id
    // ===========================================================

    @Test
    void getLoanById_ShouldReturnLoan()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/1")
                        .header("Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data.loanId")
                        .value(1));
    }

    // ===========================================================
    // TEST CASE 5
    // Loan Not Found
    // ===========================================================

    @Test
    void getLoanById_ShouldReturn404_WhenLoanNotFound()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/999")
                        .header("Authorization",
                                "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ===========================================================
    // TEST CASE 6
    // Get Loans By Customer
    // ===========================================================

    @Test
    void getLoansByCustomer_ShouldReturnSuccess()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/customer/6")
                        .header("Authorization",
                                "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    // ===========================================================
    // TEST CASE 7
    // Apply New Loan
    // ===========================================================

    @Test
    void applyLoan_ShouldReturnCreated()
            throws Exception {

        String token = obtainJwtToken();

        LoanRequestDto request = LoanRequestDto.builder()
                .customerId(6L)
                .loanType(LoanType.CAR)
                .loanAmount(new BigDecimal("500000"))
                .interestRate(new BigDecimal("9.25"))
                .tenureMonths(60)
                .remarks("Integration Test Loan")
                .build();

        mockMvc.perform(post("/api/loans")
                        .header("Authorization",
                                "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success")
                        .value(true));
    }

    // ===========================================================
    // TEST CASE 8
    // Update Pending Loan
    // ===========================================================

    @Test
    void updateLoan_ShouldReturnSuccess()
            throws Exception {

        String token = obtainJwtToken();

        LoanRequestDto request = LoanRequestDto.builder()
                .customerId(6L)
                .loanType(LoanType.CAR)
                .loanAmount(new BigDecimal("650000"))
                .interestRate(new BigDecimal("9.25"))
                .tenureMonths(72)
                .remarks("Updated by Integration Test")
                .build();

        mockMvc.perform(put("/api/loans/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Loan updated successfully."));
    }

    // ===========================================================
    // TEST CASE 9
    // Update Approved Loan
    // ===========================================================

    @Test
    void updateLoan_ShouldReturnBadRequest_WhenLoanAlreadyApproved()
            throws Exception {

        String token = obtainJwtToken();

        LoanRequestDto request = LoanRequestDto.builder()
                .customerId(6L)
                .loanType(LoanType.HOME)
                .loanAmount(new BigDecimal("900000"))
                .interestRate(new BigDecimal("8.90"))
                .tenureMonths(120)
                .remarks("Should Fail")
                .build();

        mockMvc.perform(put("/api/loans/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }



    // ===========================================================
    // TEST CASE 11
    // Process Already Approved Loan
    // ===========================================================

    @Test
    void approveLoan_ShouldReturnBadRequest_WhenAlreadyProcessed()
            throws Exception {

        String token = obtainJwtToken();

        LoanStatusUpdateDto request =
                LoanStatusUpdateDto.builder()
                        .loanStatus(LoanStatus.APPROVED)
                        .remarks("Again")
                        .build();

        mockMvc.perform(patch("/api/loans/1/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===========================================================
    // TEST CASE 12
    // Pagination
    // ===========================================================

    @Test
    void getLoansWithPagination_ShouldReturnSuccess()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===========================================================
    // TEST CASE 13
    // Filter By Status
    // ===========================================================

    @Test
    void getLoansByStatus_ShouldReturnApprovedLoans()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/status/APPROVED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===========================================================
    // TEST CASE 14
    // Filter By Loan Type
    // ===========================================================

    @Test
    void getLoansByType_ShouldReturnCarLoans()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/type/CAR")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===========================================================
    // TEST CASE 15
    // Customer Not Found
    // ===========================================================

    @Test
    void getLoansByCustomer_ShouldReturnNotFound_WhenCustomerDoesNotExist()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/customer/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ===========================================================
    // TEST CASE 16
    // Pagination With Desc Sorting
    // ===========================================================

    @Test
    void getLoansWithPagination_ShouldReturnSuccess_WithDescendingSort()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "loanAmount")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ===========================================================
    // TEST CASE 17
    // Invalid Loan Type
    // ===========================================================

    @Test
    void getLoansByType_ShouldReturnBadRequest_WhenLoanTypeIsInvalid()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/type/INVALID")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ===========================================================
    // TEST CASE 18
    // Invalid Loan Status
    // ===========================================================

    @Test
    void getLoansByStatus_ShouldReturnBadRequest_WhenStatusIsInvalid()
            throws Exception {

        String token = obtainJwtToken();

        mockMvc.perform(get("/api/loans/status/INVALID")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ===========================================================
    // TEST CASE 19
    // Validation Failure
    // ===========================================================

    @Test
    void applyLoan_ShouldReturnBadRequest_WhenMandatoryFieldsMissing()
            throws Exception {

        String token = obtainJwtToken();

        LoanRequestDto request = new LoanRequestDto();

        mockMvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===========================================================
    // TEST CASE 20
    // Loan Not Found While Updating
    // ===========================================================

    @Test
    void updateLoan_ShouldReturnNotFound_WhenLoanDoesNotExist()
            throws Exception {

        String token = obtainJwtToken();

        LoanRequestDto request = LoanRequestDto.builder()
                .customerId(6L)
                .loanType(LoanType.CAR)
                .loanAmount(new BigDecimal("500000"))
                .interestRate(new BigDecimal("9.25"))
                .tenureMonths(60)
                .remarks("Update Non Existing Loan")
                .build();

        mockMvc.perform(put("/api/loans/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

}