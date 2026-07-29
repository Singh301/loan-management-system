package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.LoanRequestDto;
import com.sudhanshu.loanmanagement.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.entity.Loan;
import com.sudhanshu.loanmanagement.entity.LoanStatus;
import com.sudhanshu.loanmanagement.entity.LoanType;
import com.sudhanshu.loanmanagement.exception.LoanAlreadyProcessedException;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.repository.LoanRepository;
import com.sudhanshu.loanmanagement.service.impl.LoanServiceImpl;
import com.sudhanshu.loanmanagement.util.EmiCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmiCalculator emiCalculator;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Customer customer;
    private Loan loan;
    private LoanRequestDto requestDto;

    @BeforeEach
    void setUp() {

        customer = Customer.builder()
                .customerId(1L)
                .firstName("Sudhanshu")
                .lastName("Singh")
                .email("abc@gmail.com")
                .mobileNumber("9876543210")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("123456789012")
                .build();

        requestDto = LoanRequestDto.builder()
                .customerId(1L)
                .loanType(LoanType.CAR)
                .loanAmount(new BigDecimal("500000"))
                .interestRate(new BigDecimal("9.5"))
                .tenureMonths(60)
                .remarks("Test Loan")
                .build();

        loan = Loan.builder()
                .loanId(100L)
                .customer(customer)
                .loanType(LoanType.CAR)
                .loanAmount(new BigDecimal("500000"))
                .interestRate(new BigDecimal("9.5"))
                .tenureMonths(60)
                .loanStatus(LoanStatus.PENDING)
                .remarks("Test Loan")
                .build();
    }

    @Test
    void applyLoan_ShouldCreateLoanSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanRepository.save(any(Loan.class)))
                .thenReturn(loan);

        LoanResponseDto response =
                loanService.applyLoan(requestDto);

        assertNotNull(response);

        assertEquals(100L, response.getLoanId());

        assertEquals(
                LoanType.CAR,
                response.getLoanType());

        assertEquals(
                LoanStatus.PENDING,
                response.getLoanStatus());

        verify(customerRepository, times(1))
                .findById(1L);

        verify(loanRepository, times(1))
                .save(any(Loan.class));
    }

    @Test
    void applyLoan_ShouldThrowException_WhenCustomerNotFound() {

        // Arrange
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> loanService.applyLoan(requestDto)
                );

        assertEquals(
                "Customer not found with id : 1",
                exception.getMessage()
        );

        verify(customerRepository, times(1))
                .findById(1L);

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void updateLoan_ShouldUpdateSuccessfully() {

        // Arrange
        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        requestDto.setLoanType(LoanType.HOME);
        requestDto.setLoanAmount(new BigDecimal("800000"));
        requestDto.setInterestRate(new BigDecimal("8.75"));
        requestDto.setTenureMonths(120);
        requestDto.setRemarks("Updated Loan");

        // Act
        LoanResponseDto response =
                loanService.updateLoan(100L, requestDto);

        // Assert
        assertNotNull(response);

        assertEquals(LoanType.HOME, response.getLoanType());

        assertEquals(
                new BigDecimal("800000"),
                response.getLoanAmount());

        assertEquals(
                new BigDecimal("8.75"),
                response.getInterestRate());

        assertEquals(
                Integer.valueOf(120),
                response.getTenureMonths());

        assertEquals(
                "Updated Loan",
                response.getRemarks());

        verify(loanRepository, times(1))
                .findById(100L);

        verify(customerRepository, times(1))
                .findById(1L);

        verify(loanRepository, times(1))
                .save(any(Loan.class));
    }

    @Test
    void updateLoan_ShouldThrowException_WhenLoanAlreadyProcessed() {

        // Arrange
        loan.setLoanStatus(LoanStatus.APPROVED);

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        // Act & Assert
        LoanAlreadyProcessedException exception =
                assertThrows(
                        LoanAlreadyProcessedException.class,
                        () -> loanService.updateLoan(100L, requestDto)
                );

        assertEquals(
                "Approved or rejected loans cannot be updated.",
                exception.getMessage()
        );

        verify(loanRepository, times(1))
                .findById(100L);

        verify(customerRepository, never())
                .findById(anyLong());

        verify(loanRepository, never())
                .save(any(Loan.class));
    }

    @Test
    void updateLoanStatus_ShouldApproveLoanSuccessfully() {

        // Arrange
        loan.setLoanStatus(LoanStatus.PENDING);

        LoanStatusUpdateDto statusDto =
                LoanStatusUpdateDto.builder()
                        .loanStatus(LoanStatus.APPROVED)
                        .remarks("Approved by manager")
                        .build();

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        when(emiCalculator.calculateEmi(
                any(BigDecimal.class),
                any(BigDecimal.class),
                anyInt()))
                .thenReturn(new BigDecimal("10545.75"));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoanResponseDto response =
                loanService.updateLoanStatus(100L, statusDto);

        // Assert
        assertNotNull(response);

        assertEquals(
                LoanStatus.APPROVED,
                response.getLoanStatus());

        assertEquals(
                new BigDecimal("10545.75"),
                response.getEmi());

        assertEquals(
                "Approved by manager",
                response.getRemarks());

        verify(loanRepository)
                .findById(100L);

        verify(emiCalculator)
                .calculateEmi(
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        anyInt());

        verify(loanRepository)
                .save(any(Loan.class));
    }

    @Test
    void updateLoanStatus_ShouldRejectLoanSuccessfully() {

        loan.setLoanStatus(LoanStatus.PENDING);

        LoanStatusUpdateDto statusDto = LoanStatusUpdateDto.builder()
                .loanStatus(LoanStatus.REJECTED)
                .remarks("Rejected due to low credit score")
                .build();

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponseDto response =
                loanService.updateLoanStatus(100L, statusDto);

        assertEquals(LoanStatus.REJECTED, response.getLoanStatus());
        assertNull(response.getEmi());
        assertEquals("Rejected due to low credit score", response.getRemarks());

        verify(emiCalculator, never())
                .calculateEmi(any(), any(), anyInt());

        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void updateLoanStatus_ShouldThrowException_WhenLoanNotFound() {

        LoanStatusUpdateDto statusDto = LoanStatusUpdateDto.builder()
                .loanStatus(LoanStatus.APPROVED)
                .remarks("Approved")
                .build();

        when(loanRepository.findById(100L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> loanService.updateLoanStatus(100L, statusDto)
                );

        assertEquals(
                "Loan not found with id : 100",
                exception.getMessage()
        );

        verify(loanRepository, never()).save(any());
    }

    @Test
    void updateLoanStatus_ShouldThrowException_WhenLoanAlreadyProcessed() {

        loan.setLoanStatus(LoanStatus.APPROVED);

        LoanStatusUpdateDto statusDto = LoanStatusUpdateDto.builder()
                .loanStatus(LoanStatus.REJECTED)
                .remarks("Rejected")
                .build();

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        LoanAlreadyProcessedException exception =
                assertThrows(
                        LoanAlreadyProcessedException.class,
                        () -> loanService.updateLoanStatus(100L, statusDto)
                );

        assertEquals(
                "Loan has already been processed.",
                exception.getMessage()
        );

        verify(loanRepository, never()).save(any());
    }

    @Test
    void updateLoanStatus_ShouldThrowException_WhenStatusIsPending() {

        loan.setLoanStatus(LoanStatus.PENDING);

        LoanStatusUpdateDto statusDto = LoanStatusUpdateDto.builder()
                .loanStatus(LoanStatus.PENDING)
                .remarks("Invalid")
                .build();

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> loanService.updateLoanStatus(100L, statusDto)
                );

        assertEquals(
                "Loan status cannot be updated to PENDING.",
                exception.getMessage()
        );

        verify(loanRepository, never()).save(any());
    }

    @Test
    void getLoanById_ShouldReturnLoanSuccessfully() {

        when(loanRepository.findById(100L))
                .thenReturn(Optional.of(loan));

        LoanResponseDto response =
                loanService.getLoanById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getLoanId());
        assertEquals(LoanType.CAR, response.getLoanType());

        verify(loanRepository).findById(100L);
    }

    @Test
    void getLoanById_ShouldThrowException_WhenLoanNotFound() {

        when(loanRepository.findById(100L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> loanService.getLoanById(100L)
                );

        assertEquals(
                "Loan not found with id : 100",
                exception.getMessage()
        );

        verify(loanRepository).findById(100L);
    }

    @Test
    void getAllLoans_ShouldReturnAllLoans() {

        Loan loan2 = Loan.builder()
                .loanId(101L)
                .customer(customer)
                .loanType(LoanType.HOME)
                .loanAmount(new BigDecimal("800000"))
                .interestRate(new BigDecimal("8.5"))
                .tenureMonths(120)
                .loanStatus(LoanStatus.PENDING)
                .build();

        when(loanRepository.findAll())
                .thenReturn(List.of(loan, loan2));

        List<LoanResponseDto> response =
                loanService.getAllLoans();

        assertEquals(2, response.size());

        verify(loanRepository).findAll();
    }

    @Test
    void getLoansByCustomer_ShouldReturnLoans() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanRepository.findByCustomerCustomerId(1L))
                .thenReturn(List.of(loan));

        List<LoanResponseDto> response =
                loanService.getLoansByCustomer(1L);

        assertEquals(1, response.size());

        assertEquals(
                LoanType.CAR,
                response.get(0).getLoanType()
        );

        verify(customerRepository).findById(1L);

        verify(loanRepository)
                .findByCustomerCustomerId(1L);
    }

    @Test
    void getLoansByCustomer_ShouldThrowException_WhenCustomerNotFound() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> loanService.getLoansByCustomer(1L)
                );

        assertEquals(
                "Customer not found with id : 1",
                exception.getMessage()
        );

        verify(customerRepository).findById(1L);

        verify(loanRepository, never())
                .findByCustomerCustomerId(anyLong());
    }

    @Test
    void getLoansWithPagination_ShouldReturnDescendingOrder() {

        Pageable pageable = PageRequest.of(
                0,
                5,
                Sort.by("loanAmount").descending()
        );

        Page<Loan> page =
                new PageImpl<>(List.of(loan));

        when(loanRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<LoanResponseDto> response =
                loanService.getLoansWithPagination(
                        0,
                        5,
                        "loanAmount",
                        "desc"
                );

        assertNotNull(response);

        assertEquals(1, response.getContent().size());

        verify(loanRepository)
                .findAll(any(Pageable.class));
    }

    @Test
    void getAllLoans_ShouldReturnEmptyList() {

        when(loanRepository.findAll())
                .thenReturn(List.of());

        List<LoanResponseDto> response =
                loanService.getAllLoans();

        assertTrue(response.isEmpty());

        verify(loanRepository).findAll();
    }

    @Test
    void getLoansByCustomer_ShouldReturnEmptyList() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanRepository.findByCustomerCustomerId(1L))
                .thenReturn(List.of());

        List<LoanResponseDto> response =
                loanService.getLoansByCustomer(1L);

        assertNotNull(response);

        assertTrue(response.isEmpty());

        verify(customerRepository)
                .findById(1L);

        verify(loanRepository)
                .findByCustomerCustomerId(1L);
    }

    @Test
    void getLoansByStatus_ShouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 5);

        Page<Loan> page =
                Page.empty(pageable);

        when(loanRepository.findByLoanStatus(
                LoanStatus.REJECTED,
                pageable))
                .thenReturn(page);

        Page<LoanResponseDto> response =
                loanService.getLoansByStatus(
                        LoanStatus.REJECTED,
                        0,
                        5
                );

        assertTrue(response.isEmpty());

        verify(loanRepository)
                .findByLoanStatus(
                        LoanStatus.REJECTED,
                        pageable);
    }

    @Test
    void getLoansByType_ShouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 5);

        Page<Loan> page =
                Page.empty(pageable);

        when(loanRepository.findByLoanType(
                LoanType.EDUCATION,
                pageable))
                .thenReturn(page);

        Page<LoanResponseDto> response =
                loanService.getLoansByType(
                        LoanType.EDUCATION,
                        0,
                        5
                );

        assertTrue(response.isEmpty());

        verify(loanRepository)
                .findByLoanType(
                        LoanType.EDUCATION,
                        pageable);
    }

    @Test
    void getLoansWithPagination_ShouldReturnSecondPage() {

        Pageable pageable = PageRequest.of(
                1,
                2,
                Sort.by("loanId").ascending()
        );

        Page<Loan> page =
                new PageImpl<>(
                        List.of(loan),
                        pageable,
                        3
                );

        when(loanRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<LoanResponseDto> response =
                loanService.getLoansWithPagination(
                        1,
                        2,
                        "loanId",
                        "asc"
                );

        assertEquals(1, response.getNumber());

        assertEquals(2, response.getSize());

        verify(loanRepository)
                .findAll(any(Pageable.class));
    }

}