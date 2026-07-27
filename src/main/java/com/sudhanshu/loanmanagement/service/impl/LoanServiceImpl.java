package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.entity.Loan;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;

    private final CustomerRepository customerRepository;

    @Override
    public LoanResponseDto applyLoan(LoanRequestDto requestDto) {

        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : "
                                        + requestDto.getCustomerId()));

        Loan loan = Loan.builder()
                .customer(customer)
                .loanType(requestDto.getLoanType())
                .loanAmount(requestDto.getLoanAmount())
                .interestRate(requestDto.getInterestRate())
                .tenureMonths(requestDto.getTenureMonths())
                .remarks(requestDto.getRemarks())
                .loanStatus(LoanStatus.PENDING)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        return LoanResponseDto.builder()
                .loanId(savedLoan.getLoanId())
                .customerId(savedLoan.getCustomer().getCustomerId())
                .loanType(savedLoan.getLoanType())
                .loanAmount(savedLoan.getLoanAmount())
                .interestRate(savedLoan.getInterestRate())
                .tenureMonths(savedLoan.getTenureMonths())
                .emi(savedLoan.getEmi())
                .loanStatus(savedLoan.getLoanStatus())
                .applicationDate(savedLoan.getApplicationDate())
                .remarks(savedLoan.getRemarks())
                .build();
    }

}