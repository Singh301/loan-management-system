package com.sudhanshu.loanmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sudhanshu.loanmanagement.dto.LoginRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // Test Case 1
    // Valid Login
    // ===============================

    @Test
    void login_ShouldReturnSuccess_WhenCredentialsAreValid() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("sudhanshu");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Authentication successful"))
                .andExpect(jsonPath("$.data.username")
                        .value("sudhanshu"))
                .andExpect(jsonPath("$.data.token")
                        .isNotEmpty());
    }

    // ===============================
    // Test Case 2
    // Wrong Password
    // ===============================

    @Test
    void login_ShouldReturnUnauthorized_WhenPasswordIsWrong() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("sudhanshu");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ===============================
    // Test Case 3
    // Wrong Username
    // ===============================

    @Test
    void login_ShouldReturnUnauthorized_WhenUsernameIsWrong() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("unknown");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ===============================
    // Test Case 4
    // Empty Username
    // ===============================

    @Test
    void login_ShouldReturnBadRequest_WhenUsernameIsEmpty() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // Test Case 5
    // Empty Password
    // ===============================

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordIsEmpty() throws Exception {

        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("sudhanshu");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // Test Case 6
    // Empty Request
    // ===============================

    @Test
    void login_ShouldReturnBadRequest_WhenRequestIsEmpty() throws Exception {

        LoginRequestDto request = new LoginRequestDto();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // Test Case 7
    // Invalid JSON
    // ===============================

    @Test
    void login_ShouldReturnBadRequest_WhenJsonIsInvalid() throws Exception {

        String json = "{invalid-json}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // Test Case 8
    // Missing Request Body
    // ===============================

    @Test
    void login_ShouldReturnBadRequest_WhenBodyIsMissing() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}