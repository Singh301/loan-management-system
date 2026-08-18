package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;
import com.sudhanshu.loanmanagement.repayment.dto.LoanRepaymentResponseDto;
import com.sudhanshu.loanmanagement.repayment.entity.LoanRepayment;
import com.sudhanshu.loanmanagement.exception.AccessDeniedException;
import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.loan.dto.*;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import com.sudhanshu.loanmanagement.loan.repository.LoanAnalyticsProjection;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.audit.service.AuditService;
import com.sudhanshu.loanmanagement.loan.service.LoanService;
import com.sudhanshu.loanmanagement.loan.service.LoanApplicationService;
import com.sudhanshu.loanmanagement.loan.service.LoanApprovalService;
import com.sudhanshu.loanmanagement.loan.service.LoanDisbursementService;
import com.sudhanshu.loanmanagement.loan.service.EmiScheduleService;
import com.sudhanshu.loanmanagement.loan.mapper.LoanMapper;
import com.sudhanshu.loanmanagement.loan.domain.LoanStateMachine;
import com.sudhanshu.loanmanagement.security.OwnershipGuard;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.sudhanshu.loanmanagement.loan.specification.LoanSpecification;

import com.sudhanshu.loanmanagement.loan.dto.DisburseLoanRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.EmiScheduleResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule.EmiStatus;
import com.sudhanshu.loanmanagement.loan.entity.LoanStatus;
import com.sudhanshu.loanmanagement.loan.repository.EmiScheduleRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import java.math.RoundingMode;
import java.util.ArrayList;

import java.math.BigDecimal;
import java.util.List;

import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;

import com.sudhanshu.loanmanagement.repayment.repository.LoanRepaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private static final Logger logger =
            LoggerFactory.getLogger(LoanServiceImpl.class);

    private final LoanRepository loanRepository;

    private final LoanRepaymentRepository loanRepaymentRepository;

    private final CustomerRepository customerRepository;

    private final AuditService auditService;

    private final EmiCalculator emiCalculator;

    private final EmiScheduleRepository emiScheduleRepository;

    private final NotificationService notificationService;

    // Phase 1 – focused services
    private final LoanApplicationService loanApplicationService;
    private final LoanApprovalService loanApprovalService;
    private final LoanDisbursementService loanDisbursementService;
    private final EmiScheduleService emiScheduleService;
    private final LoanMapper loanMapper;
    private final LoanStateMachine loanStateMachine;
    private final OwnershipGuard ownershipGuard;



    @Override
    public LoanResponseDto applyLoan(LoanRequestDto requestDto) {
        return loanApplicationService.applyLoan(requestDto);
    }

    
    @Override
    public List<LoanResponseDto> getAllLoans(LoanType loanType) {

        logger.info("Fetching loans. loanType={}", loanType);

        List<Loan> loans;

        if (loanType == null) {
            loans = loanRepository.findAll();
        } else {
            loans = loanRepository.findByLoanType(loanType);
        }

        List<LoanResponseDto> response = loans.stream()
                .map(this::mapToResponseDto)
                .toList();

        logger.info(
                "Loans fetched successfully. loanType={}, totalRecords={}",
                loanType,
                response.size()
        );

        return response;
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
        ownershipGuard.assertCanAccessCustomer(customerId);

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
    public List<LoanResponseDto> getMyLoans(String username) {

        Customer customer = customerRepository
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found for logged in user."
                        ));

        return loanRepository
                .findByCustomerCustomerId(customer.getCustomerId())
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public LoanResponseDto getMyLoan(
            Long loanId,
            String username) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        ownershipGuard.assertCanAccessLoan(loan);
        // username param kept for API compatibility; guard uses SecurityContext

        return mapToResponseDto(loan);
    }

    @Override
    public LoanResponseDto updateLoan(Long loanId, LoanRequestDto requestDto) {
        return loanApplicationService.updateLoan(loanId, requestDto);
    }

    
    @Override
    @Transactional
    public LoanResponseDto updateLoanStatus(Long loanId, LoanStatusUpdateDto requestDto) {
        // Approver identity will be wired from SecurityContext in controller later.
        // For now use SYSTEM as placeholder; multi-level logic still works with level resolution.
        return loanApprovalService.updateLoanStatus(loanId, requestDto, 0L, "ROLE_ADMIN");
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

    @Override
    public Page<LoanResponseDto> searchLoans(
            String customerName,
            String email,
            LoanType loanType,
            LoanStatus loanStatus,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int page,
            int size,
            String sortBy,
            String direction) {


        logger.info(
                "Entering searchLoans: customerName={}, email={}, loanType={}, loanStatus={}, minAmount={}, maxAmount={}",
                customerName,
                email,
                loanType,
                loanStatus,
                minAmount,
                maxAmount
        );


        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return loanRepository.findAll(
                        LoanSpecification.search(
                                customerName,
                                email,
                                loanType,
                                loanStatus,
                                minAmount,
                                maxAmount),
                        pageable)
                .map(this::mapToResponseDto);
    }

    @Override
    public CustomerLoanHistoryResponseDto getCustomerLoanHistory(Long customerId) {

        logger.info("Fetching loan history for customerId={}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id : " + customerId));

        List<LoanResponseDto> loans = loanRepository
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();

        CustomerLoanHistoryResponseDto response =
                CustomerLoanHistoryResponseDto.builder()
                        .customerId(customer.getCustomerId())
                        .customerName(
                                customer.getFirstName() + " " + customer.getLastName())
                        .email(customer.getEmail())
                        .mobileNumber(customer.getMobileNumber())

                        .totalLoans(
                                loanRepository.countByCustomerCustomerId(customerId))

                        .approvedLoans(
                                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                                        customerId,
                                        LoanStatus.APPROVED))

                        .pendingLoans(
                                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                                        customerId,
                                        LoanStatus.PENDING))

                        .rejectedLoans(
                                loanRepository.countByCustomerCustomerIdAndLoanStatus(
                                        customerId,
                                        LoanStatus.REJECTED))

                        .loans(loans)
                        .build();

        logger.info(
                "Loan history fetched successfully. customerId={}, totalLoans={}",
                customerId,
                response.getTotalLoans());

        return response;
    }

    @Override
    public LoanStatisticsResponseDto getLoanStatistics() {

        logger.info("Fetching loan statistics.");

        LoanStatisticsResponseDto response =
                LoanStatisticsResponseDto.builder()

                        .totalLoans(
                                loanRepository.count())

                        .approvedLoans(
                                loanRepository.countByLoanStatus(
                                        LoanStatus.APPROVED))

                        .pendingLoans(
                                loanRepository.countByLoanStatus(
                                        LoanStatus.PENDING))

                        .rejectedLoans(
                                loanRepository.countByLoanStatus(
                                        LoanStatus.REJECTED))

                        .totalLoanAmount(
                                loanRepository.getTotalLoanAmount())

                        .approvedLoanAmount(
                                loanRepository.getTotalApprovedLoanAmount())

                        .averageLoanAmount(
                                loanRepository.getAverageLoanAmount())

                        .highestLoanAmount(
                                loanRepository.getHighestLoanAmount())

                        .lowestLoanAmount(
                                loanRepository.getLowestLoanAmount())

                        .build();

        logger.info("Loan statistics fetched successfully.");

        return response;
    }

    @Override
    public List<LoanMonthlyReportDto> getMonthlyLoanReport() {

        logger.info("Fetching monthly loan report.");

        List<LoanMonthlyReportDto> report =
                loanRepository.getMonthlyLoanReport();

        logger.info(
                "Monthly report generated. Total months={}",
                report.size());

        return report;
    }

    @Override
    public LoanAnalyticsResponseDto getLoanAnalytics(
            LoanType loanType) {

        logger.info(
                "Fetching loan analytics. loanType={}",
                loanType
        );

        LoanAnalyticsProjection result =
                loanRepository.getLoanAnalyticsByType(loanType);

        return LoanAnalyticsResponseDto.builder()
                .loanType(loanType)
                .totalLoans(
                        result.getTotalLoans() == null
                                ? 0L
                                : result.getTotalLoans()
                )
                .totalAmount(
                        result.getTotalAmount() == null
                                ? BigDecimal.ZERO
                                : result.getTotalAmount()
                )
                .averageLoanAmount(
                        result.getAverageLoanAmount() == null
                                ? BigDecimal.ZERO
                                : result.getAverageLoanAmount()
                )
                .build();
    }

    @Override
    public List<LoanResponseDto> getLoansByType(LoanType loanType) {

        logger.info(
                "Fetching loans by type. loanType={}",
                loanType
        );

        List<LoanResponseDto> loans = loanRepository
                .findByLoanType(loanType)
                .stream()
                .map(this::mapToResponseDto)
                .toList();

        log.info(
                "Loan type filter completed. loanType={}, totalRecords={}",
                loanType,
                loans.size()
        );

        return loans;
    }

    @Override
    public List<EmiScheduleResponseDto> generateEmiSchedule(Long loanId) {
        return emiScheduleService.previewEmiSchedule(loanId);
    }

    
    @Override
    public Page<LoanResponseDto> getAllLoansPaginated(
            LoanType loanType,
            int page,
            int size,
            String sortBy,
            String direction) {

        logger.info(
                "Fetching paginated loans. loanType={}, page={}, size={}, sortBy={}, direction={}",
                loanType, page, size, sortBy, direction
        );

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Loan> loanPage;

        if (loanType == null) {
            loanPage = loanRepository.findAll(pageable);
        } else {
            loanPage = loanRepository.findByLoanType(loanType, pageable);
        }

        return loanPage.map(this::mapToResponseDto);
    }

    @Override
    public LoanForeclosureResponseDto getForeclosureDetails(

            Long loanId,

            Integer paidInstallments) {

        logger.info("Calculating foreclosure amount for loanId={}", loanId);

        Loan loan = loanRepository.findById(loanId)

                .orElseThrow(() ->

                        new ResourceNotFoundException(

                                "Loan not found with id : " + loanId));

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {

            throw new IllegalArgumentException(

                    "Foreclosure is allowed only for approved loans.");
        }

        if (paidInstallments < 0 ||

                paidInstallments > loan.getTenureMonths()) {

            throw new IllegalArgumentException(

                    "Invalid paid installments.");
        }

        BigDecimal balance = loan.getLoanAmount();

        BigDecimal monthlyRate =

                loan.getInterestRate()

                        .divide(BigDecimal.valueOf(1200),

                                10,

                                RoundingMode.HALF_UP);

        BigDecimal emi = loan.getEmi();

        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (int i = 1;

             i <= paidInstallments;

             i++) {

            BigDecimal interest =

                    balance.multiply(monthlyRate)

                            .setScale(2,

                                    RoundingMode.HALF_UP);

            BigDecimal principal =

                    emi.subtract(interest);

            balance = balance.subtract(principal)

                    .setScale(2,

                            RoundingMode.HALF_UP);

            totalInterestPaid =

                    totalInterestPaid.add(interest);
        }

        BigDecimal totalInterest =

                emi.multiply(

                                BigDecimal.valueOf(

                                        loan.getTenureMonths()))

                        .subtract(

                                loan.getLoanAmount());

        BigDecimal interestSaved =

                totalInterest.subtract(totalInterestPaid);

        if (interestSaved.compareTo(BigDecimal.ZERO) < 0) {

            interestSaved = BigDecimal.ZERO;
        }

        BigDecimal foreclosureCharges =

                balance.multiply(

                                BigDecimal.valueOf(0.02))

                        .setScale(2,

                                RoundingMode.HALF_UP);

        BigDecimal settlement =

                balance.add(foreclosureCharges);

        return LoanForeclosureResponseDto.builder()

                .loanId(loan.getLoanId())

                .remainingPrincipal(balance)

                .interestSaved(interestSaved)

                .foreclosureCharges(foreclosureCharges)

                .totalSettlementAmount(settlement)

                .build();
    }

    @Override
    public LoanStatementResponseDto getLoanStatement(Long loanId) {

        log.info("Fetching loan statement. loanId={}", loanId);

        Loan loan = loanRepository.findByIdWithCustomer(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id : " + loanId));

        List<LoanRepaymentResponseDto> repayments =
                loanRepaymentRepository
                        .findByLoanLoanIdOrderByPaymentDateDesc(loanId)
                        .stream()
                        .map(this::mapRepayment)
                        .toList();

        Customer customer = loan.getCustomer();

        return LoanStatementResponseDto.builder()

                .loanId(loan.getLoanId())

                .customerId(customer.getCustomerId())

                .customerName(
                        customer.getFirstName()
                                + " "
                                + customer.getLastName())

                .email(customer.getEmail())

                .mobileNumber(customer.getMobileNumber())

                .loanType(loan.getLoanType())

                .loanStatus(loan.getLoanStatus())

                .loanAmount(loan.getLoanAmount())

                .interestRate(loan.getInterestRate())

                .tenureMonths(loan.getTenureMonths())

                .emi(loan.getEmi())

                .outstandingPrincipal(
                        loan.getOutstandingPrincipal())

                .paidInstallments(
                        loan.getPaidInstallments())

                .remainingInstallments(
                        loan.getRemainingInstallments())

                .totalAmountPaid(
                        loanRepaymentRepository
                                .sumAmountPaidByLoanLoanId(loanId))

                .totalPrincipalPaid(
                        loanRepaymentRepository
                                .sumPrincipalPaidByLoanLoanId(loanId))

                .totalInterestPaid(
                        loanRepaymentRepository
                                .sumInterestPaidByLoanLoanId(loanId))

                .repayments(repayments)

                .build();
    }


    private LoanResponseDto mapToResponseDto(Loan loan) {
        return loanMapper.toResponseDto(loan);
    }

    private LoanRepaymentResponseDto mapRepayment(
            LoanRepayment repayment) {

        return LoanRepaymentResponseDto.builder()
                .repaymentId(repayment.getRepaymentId())
                .loanId(repayment.getLoan().getLoanId())
                .amountPaid(repayment.getAmountPaid())
                .principalPaid(repayment.getPrincipalPaid())
                .interestPaid(repayment.getInterestPaid())
                .remainingPrincipal(repayment.getRemainingPrincipal())
                .paymentDate(repayment.getPaymentDate())
                .paymentMode(repayment.getPaymentMode())
                .transactionReference(repayment.getTransactionReference())
                .remarks(repayment.getRemarks())
                .build();
    }

    private List<EmiSchedule> generateAndSaveEmiSchedule(Loan loan) {

        List<EmiSchedule> schedules = new ArrayList<>();

        BigDecimal principal = loan.getLoanAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        int tenure = loan.getTenureMonths();
        BigDecimal emi = loan.getEmi();

        BigDecimal outstanding = principal;
        LocalDate dueDate = loan.getDisbursementDate().plusMonths(1);

        for (int i = 1; i <= tenure; i++) {

            BigDecimal interestComponent = outstanding
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalComponent = emi.subtract(interestComponent)
                    .setScale(2, RoundingMode.HALF_UP);

            // Adjustment for last installment
            if (i == tenure) {
                principalComponent = outstanding;
                emi = principalComponent.add(interestComponent);
            }

            outstanding = outstanding.subtract(principalComponent)
                    .setScale(2, RoundingMode.HALF_UP);

            if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                outstanding = BigDecimal.ZERO;
            }

            EmiSchedule schedule = EmiSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(dueDate)
                    .emiAmount(emi)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .outstandingPrincipalAfter(outstanding)
                    .status(EmiStatus.PENDING)
                    .build();

            schedules.add(schedule);
            dueDate = dueDate.plusMonths(1);
        }

        return emiScheduleRepository.saveAll(schedules);
    }

    @Override
    @Transactional
    public LoanResponseDto disburseLoan(Long loanId, DisburseLoanRequestDto request) {
        // Idempotency key can be passed via header later; null is accepted
        return loanDisbursementService.disburseLoan(loanId, request, null);
    }

    
    @Override
    public List<EmiScheduleResponseDto> getEmiScheduleFromDb(Long loanId) {
        return emiScheduleService.getEmiSchedule(loanId);
    }

    
    @Override
    @Transactional
    public void markOverdueLoans() {
        emiScheduleService.markOverdueLoans();
    }


}




