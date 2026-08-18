package com.sudhanshu.loanmanagement.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmiCalculatorTest {

    private EmiCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EmiCalculator();
    }

    @Test
    @DisplayName("Standard EMI calculation for 10L @ 10% for 12 months")
    void calculateEmi_standardCase() {
        BigDecimal emi = calculator.calculateEmi(new BigDecimal("1000000"), new BigDecimal("10"), 12);
        assertNotNull(emi);
        assertEquals(2, emi.scale());
        assertTrue(emi.compareTo(new BigDecimal("87000")) > 0);
        assertTrue(emi.compareTo(new BigDecimal("89000")) < 0);
    }

    @Test
    @DisplayName("Zero interest rate returns principal / tenure")
    void calculateEmi_zeroInterest() {
        BigDecimal emi = calculator.calculateEmi(new BigDecimal("120000"), BigDecimal.ZERO, 12);
        assertEquals(new BigDecimal("10000.00"), emi);
    }

    @Test
    @DisplayName("Rejects non-positive principal")
    void calculateEmi_invalidPrincipal() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateEmi(BigDecimal.ZERO, new BigDecimal("10"), 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateEmi(new BigDecimal("-100"), new BigDecimal("10"), 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateEmi(null, new BigDecimal("10"), 12));
    }

    @Test
    @DisplayName("Rejects negative rate or invalid tenure")
    void calculateEmi_invalidRateOrTenure() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateEmi(new BigDecimal("100000"), new BigDecimal("-1"), 12));
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateEmi(new BigDecimal("100000"), new BigDecimal("10"), 0));
    }

    @Test
    @DisplayName("Schedule final installment clears principal exactly")
    void generateSchedule_finalInstallmentClearsPrincipal() {
        List<EmiCalculator.EmiInstallment> schedule =
                calculator.generateSchedule(new BigDecimal("100000"), new BigDecimal("12"), 6);
        assertEquals(6, schedule.size());
        EmiCalculator.EmiInstallment last = schedule.get(schedule.size() - 1);
        assertEquals(0, last.outstandingPrincipal().compareTo(BigDecimal.ZERO));
        BigDecimal totalPrincipal = schedule.stream()
                .map(EmiCalculator.EmiInstallment::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalPrincipal.compareTo(new BigDecimal("100000")));
    }

    @ParameterizedTest
    @CsvSource({"500000, 8.5, 24", "250000, 11, 36", "1000000, 9.75, 60"})
    @DisplayName("EMI is positive and has scale 2")
    void calculateEmi_variousInputs(String p, String r, int n) {
        BigDecimal emi = calculator.calculateEmi(new BigDecimal(p), new BigDecimal(r), n);
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
        assertEquals(2, emi.scale());
    }
}
