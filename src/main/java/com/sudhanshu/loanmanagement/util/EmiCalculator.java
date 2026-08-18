package com.sudhanshu.loanmanagement.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * EMI calculator using pure BigDecimal arithmetic for monetary correctness.
 * Avoids floating-point precision issues inherent in double.
 *
 * Formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 * where r = monthly interest rate, n = tenure in months.
 */
@Component
public class EmiCalculator {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    public BigDecimal calculateEmi(BigDecimal principal,
                                   BigDecimal annualInterestRate,
                                   Integer tenureMonths) {
        validateInputs(principal, annualInterestRate, tenureMonths);

        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, MONEY_ROUNDING);
        }

        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), MC);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal power = onePlusR.pow(tenureMonths, MC);
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(power, MC);
        BigDecimal denominator = power.subtract(BigDecimal.ONE, MC);

        return numerator.divide(denominator, MONEY_SCALE, MONEY_ROUNDING);
    }

    public List<EmiInstallment> generateSchedule(BigDecimal principal,
                                                 BigDecimal annualInterestRate,
                                                 Integer tenureMonths) {
        validateInputs(principal, annualInterestRate, tenureMonths);

        BigDecimal emi = calculateEmi(principal, annualInterestRate, tenureMonths);
        BigDecimal monthlyRate = annualInterestRate.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : annualInterestRate.divide(BigDecimal.valueOf(1200), MC);

        List<EmiInstallment> schedule = new ArrayList<>();
        BigDecimal remaining = principal;

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interest = remaining.multiply(monthlyRate, MC).setScale(MONEY_SCALE, MONEY_ROUNDING);
            BigDecimal principalComponent;
            BigDecimal currentEmi = emi;

            if (i == tenureMonths) {
                principalComponent = remaining;
                currentEmi = principalComponent.add(interest).setScale(MONEY_SCALE, MONEY_ROUNDING);
            } else {
                principalComponent = emi.subtract(interest).setScale(MONEY_SCALE, MONEY_ROUNDING);
                if (principalComponent.compareTo(remaining) > 0) {
                    principalComponent = remaining;
                }
            }

            remaining = remaining.subtract(principalComponent).setScale(MONEY_SCALE, MONEY_ROUNDING);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }

            schedule.add(new EmiInstallment(i, currentEmi, interest, principalComponent, remaining));
        }
        return schedule;
    }

    private void validateInputs(BigDecimal principal, BigDecimal annualInterestRate, Integer tenureMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual interest rate cannot be negative");
        }
        if (tenureMonths == null || tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be a positive integer");
        }
    }

    public record EmiInstallment(
            int installmentNumber,
            BigDecimal emi,
            BigDecimal interestComponent,
            BigDecimal principalComponent,
            BigDecimal outstandingPrincipal
    ) {}
}
