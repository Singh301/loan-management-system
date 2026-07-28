package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.entity.Loan;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<LoanResponseDto> getAllLoans() {

        return loanRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public LoanResponseDto getLoanById(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        return mapToResponseDto(loan);
    }

    @Override
    public List<LoanResponseDto> getLoansByCustomer(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        return loanRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private LoanResponseDto mapToResponseDto(Loan loan) {

        return LoanResponseDto.builder()
                .loanId(loan.getLoanId())
                .customerId(loan.getCustomer().getCustomerId())
                .loanType(loan.getLoanType())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emi(loan.getEmi())
                .loanStatus(loan.getLoanStatus())
                .applicationDate(loan.getApplicationDate())
                .remarks(loan.getRemarks())
                .build();
    }

    @Override
    public LoanResponseDto updateLoan(Long loanId,
                                      LoanRequestDto requestDto) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new LoanAlreadyProcessedException(
                    "Approved or rejected loans cannot be updated.");
        }

        Customer customer = customerRepository.findById(
                        requestDto.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : "
                                        + requestDto.getCustomerId()));

        loan.setCustomer(customer);
        loan.setLoanType(requestDto.getLoanType());
        loan.setLoanAmount(requestDto.getLoanAmount());
        loan.setInterestRate(requestDto.getInterestRate());
        loan.setTenureMonths(requestDto.getTenureMonths());
        loan.setRemarks(requestDto.getRemarks());

        Loan updatedLoan = loanRepository.save(loan);

        return mapToResponseDto(updatedLoan);
    }

}