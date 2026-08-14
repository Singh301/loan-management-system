package com.sudhanshu.loanmanagement.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Loan Management System is Running Successfully";
    }
}




