package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.entity.Loan;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.LoanService;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private static final Logger logger =
            LoggerFactory.getLogger(LoanServiceImpl.class);

    private final LoanRepository loanRepository;

    private final CustomerRepository customerRepository;

    private final EmiCalculator emiCalculator;

    @Override
    public LoanResponseDto applyLoan(LoanRequestDto requestDto) {

        logger.info(
                "Loan application requested. customerId={}, loanType={}, amount={}",
                requestDto.getCustomerId(),
                requestDto.getLoanType(),
                requestDto.getLoanAmount()
        );

        Customer customer = customerRepository.findById(
                        requestDto.getCustomerId())
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

        logger.info(
                "Loan created successfully. loanId={}, customerId={}",
                savedLoan.getLoanId(),
                savedLoan.getCustomer().getCustomerId()
        );

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

        logger.info("Fetching all loans.");

        List<LoanResponseDto> loans = loanRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();

        logger.info("Total loans fetched: {}", loans.size());

        return loans;
    }

    @Override
    public LoanResponseDto getLoanById(Long loanId) {

        logger.info("Fetching loan details. loanId={}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        logger.debug(
                "Loan found. loanId={}, status={}",
                loan.getLoanId(),
                loan.getLoanStatus()
        );

        return mapToResponseDto(loan);
    }

    @Override
    public List<LoanResponseDto> getLoansByCustomer(Long customerId) {

        logger.info("Fetching loans for customerId={}", customerId);

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        List<LoanResponseDto> loans = loanRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();

        logger.info(
                "Fetched {} loan(s) for customerId={}",
                loans.size(),
                customerId
        );

        return loans;
    }

    @Override
    public LoanResponseDto updateLoan(
            Long loanId,
            LoanRequestDto requestDto) {

        logger.info(
                "Loan update requested. loanId={}, customerId={}",
                loanId,
                requestDto.getCustomerId()
        );

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {

            logger.warn(
                    "Loan update rejected. loanId={} is already {}",
                    loanId,
                    loan.getLoanStatus()
            );

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

        logger.info(
                "Loan updated successfully. loanId={}, loanType={}, amount={}",
                updatedLoan.getLoanId(),
                updatedLoan.getLoanType(),
                updatedLoan.getLoanAmount()
        );

        return mapToResponseDto(updatedLoan);
    }

    @Override
    public LoanResponseDto updateLoanStatus(
            Long loanId,
            LoanStatusUpdateDto requestDto) {

        logger.info(
                "Loan status update requested. loanId={}, newStatus={}",
                loanId,
                requestDto.getLoanStatus()
        );

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {

            logger.warn(
                    "Loan status update rejected. loanId={} already {}",
                    loanId,
                    loan.getLoanStatus()
            );

            throw new LoanAlreadyProcessedException(
                    "Loan has already been processed.");
        }

        if (requestDto.getLoanStatus() == LoanStatus.PENDING) {

            logger.warn(
                    "Invalid status update attempted. loanId={}, status=PENDING",
                    loanId
            );

            throw new IllegalArgumentException(
                    "Loan status cannot be updated to PENDING.");
        }

        loan.setLoanStatus(requestDto.getLoanStatus());

        if (requestDto.getRemarks() != null
                && !requestDto.getRemarks().isBlank()) {

            loan.setRemarks(requestDto.getRemarks());
        }

        if (requestDto.getLoanStatus() == LoanStatus.APPROVED) {

            loan.setEmi(
                    emiCalculator.calculateEmi(
                            loan.getLoanAmount(),
                            loan.getInterestRate(),
                            loan.getTenureMonths()
                    )
            );

            logger.debug(
                    "EMI calculated successfully. loanId={}, emi={}",
                    loan.getLoanId(),
                    loan.getEmi()
            );
        }

        Loan updatedLoan = loanRepository.save(loan);

        logger.info(
                "Loan status updated successfully. loanId={}, status={}, emi={}",
                updatedLoan.getLoanId(),
                updatedLoan.getLoanStatus(),
                updatedLoan.getEmi()
        );

        return mapToResponseDto(updatedLoan);
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
    public Page<LoanResponseDto> getLoansWithPagination(
            int page,
            int size,
            String sortBy,
            String direction) {

        logger.info(
                "Fetching loans with pagination. page={}, size={}, sortBy={}, direction={}",
                page,
                size,
                sortBy,
                direction
        );

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LoanResponseDto> result = loanRepository.findAll(pageable)
                .map(this::mapToResponseDto);

        logger.info(
                "Pagination completed. totalElements={}, totalPages={}, currentPage={}",
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber()
        );

        return result;
    }

    @Override
    public Page<LoanResponseDto> getLoansByStatus(
            LoanStatus loanStatus,
            int page,
            int size) {

        logger.info(
                "Fetching loans by status. status={}, page={}, size={}",
                loanStatus,
                page,
                size
        );

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanResponseDto> result =
                loanRepository.findByLoanStatus(loanStatus, pageable)
                        .map(this::mapToResponseDto);

        logger.info(
                "Status filter completed. status={}, totalRecords={}",
                loanStatus,
                result.getTotalElements()
        );

        return result;
    }

    @Override
    public Page<LoanResponseDto> getLoansByType(
            LoanType loanType,
            int page,
            int size) {

        logger.info(
                "Fetching loans by type. loanType={}, page={}, size={}",
                loanType,
                page,
                size
        );

        Pageable pageable = PageRequest.of(page, size);

        Page<LoanResponseDto> result =
                loanRepository.findByLoanType(loanType, pageable)
                        .map(this::mapToResponseDto);

        logger.info(
                "Loan type filter completed. loanType={}, totalRecords={}",
                loanType,
                result.getTotalElements()
        );

        return result;
    }
}