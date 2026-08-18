package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.event.LoanCreatedEvent;
import com.sudhanshu.loanmanagement.loan.mapper.LoanMapper;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.loan.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.sudhanshu.loanmanagement.outbox.OutboxService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanMapper loanMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public LoanResponseDto applyLoan(LoanRequestDto requestDto) {
        log.info("Loan application requested. customerId={}, loanType={}, amount={}",
                requestDto.getCustomerId(), requestDto.getLoanType(), requestDto.getLoanAmount());

        Customer customer = customerRepository.findById(requestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id : " + requestDto.getCustomerId()));

        if (Boolean.TRUE.equals(customer.getDeleted()) || !customer.isActive()) {
            throw new IllegalStateException("Cannot apply loan for inactive or deleted customer.");
        }

        Loan loan = Loan.builder()
                .customer(customer)
                .loanType(requestDto.getLoanType())
                .loanAmount(requestDto.getLoanAmount())
                .interestRate(requestDto.getInterestRate())
                .tenureMonths(requestDto.getTenureMonths())
                .remarks(requestDto.getRemarks())
                .loanStatus(LoanStatus.PENDING)
                .outstandingPrincipal(requestDto.getLoanAmount())
                .paidInstallments(0)
                .remainingInstallments(requestDto.getTenureMonths())
                .deleted(false)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        LoanCreatedEvent createdEvent = new LoanCreatedEvent(
                savedLoan.getLoanId(),
                savedLoan.getCustomer().getCustomerId(),
                savedLoan.getLoanType(),
                savedLoan.getLoanAmount()
        );
        eventPublisher.publishEvent(createdEvent);
        outboxService.enqueue("LOAN", String.valueOf(savedLoan.getLoanId()),
                "LoanCreated", createdEvent);

        log.info("Loan created successfully. loanId={}", savedLoan.getLoanId());
        return loanMapper.toResponseDto(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto) {
        log.info("Loan update requested. loanId={}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id : " + loanId));

        if (Boolean.TRUE.equals(loan.getDeleted())) {
            throw new ResourceNotFoundException("Loan not found with id : " + loanId);
        }

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            log.warn("Loan update rejected. loanId={} is already {}", loanId, loan.getLoanStatus());
            throw new LoanAlreadyProcessedException(
                    "Approved or rejected loans cannot be updated.");
        }

        loan.setLoanType(requestDto.getLoanType());
        loan.setLoanAmount(requestDto.getLoanAmount());
        loan.setInterestRate(requestDto.getInterestRate());
        loan.setTenureMonths(requestDto.getTenureMonths());
        loan.setOutstandingPrincipal(requestDto.getLoanAmount());
        loan.setRemainingInstallments(requestDto.getTenureMonths());

        if (requestDto.getRemarks() != null) {
            loan.setRemarks(requestDto.getRemarks());
        }

        Loan updated = loanRepository.save(loan);
        log.info("Loan updated successfully. loanId={}", loanId);
        return loanMapper.toResponseDto(updated);
    }
}
